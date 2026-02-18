package org.cytraining.backend;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {

    public static void main(String[] args) {
        // Setup the logger
        // TODO to it in a singleton
        Logger log = LoggerFactory.getLogger(Main.class);

        // Load .env
        // TODO do it in a singleton
        Dotenv dotenv = Dotenv.load();

        String db_host = dotenv.get("DATABASE_HOST");
        String db_port = dotenv.get("DATABASE_PORT");
        String db_name = dotenv.get("DATABASE_NAME");
        String db_user = dotenv.get("DATABASE_USER");
        String db_pass = dotenv.get("DATABASE_PASS");

        int server_port = Integer.parseInt(dotenv.get("BACKEND_PORT"));

        // a bit weird here:
        // APP_MODE is only for development, it only serve to emulate which mode is used
        // when launching the server
        // it is overriden by "System.getProperty("env")", when you build for prod.
        // Setting anything in the mode property will make the program believe it's in
        // production mode
        boolean dev_mode = System.getProperty("mode").isEmpty() && dotenv.get("APP_MODE") != "prod";

        if (dev_mode) {
            log.warn("This instance is in development mode. Do not use it for production!");
        } else {
            log.info("This instance is in production mode.");
        }

        // Build the PostgreSQL url
        String db_url = "jdbc:postgresql://" + db_host + ":" + db_port + "/" + db_name;

        // setup HikariCP for ProgreSQL connection pool
        // TODO setup in a singleton, like https://www.baeldung.com/hikaricp
        HikariConfig hc = new HikariConfig();

        hc.setJdbcUrl(db_url);
        hc.setUsername(db_user);
        hc.setPassword(db_pass);
        hc.addDataSourceProperty("cachePrepStmts", "true");
        hc.addDataSourceProperty("prepStmtCacheSize", "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hc.addDataSourceProperty("tcpKeepAlive", "true");
        HikariDataSource ds = new HikariDataSource(hc);

        // Test connection
        try {
            Connection connection = ds.getConnection();
            log.info("Successfully connect to PostgreSQL server");
            connection.close();
        } catch (SQLException e) {
            log.error(MarkerFactory.getMarker("FATAL"), "Failed to connecto to PostgreSQL server:", e);
            ds.close();
            return;
        }

        // Javalin app
        Javalin app = Javalin.create(config -> {
            // allow specific host on the api (CORS Access-Control-Allow-Origin)
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    if (dev_mode) {
                        log.warn(
                                "Development mode enabled, allowing any host. This should not be used is production mode. See the .env file.");
                        it.anyHost();
                    } else {
                        // enable only the website to make request
                        it.allowHost("127.0.0.1");
                    }
                });
            });

            if (dev_mode) {
                // for in depth automatic logging on each request
                // config.bundledPlugins.enableDevLogging();

                config.requestLogger.http((ctx, ms) -> {
                    // development logging here
                    log.info("[ " + ctx.ip() + "\t] " + ctx.url() + ": " + ctx.res().getStatus());
                });
            } else {
                config.showJavalinBanner = false;
                config.requestLogger.http((ctx, ms) -> {
                    // production logging
                });

                // will serve the built static frontend files
                // this is for all assets
                config.staticFiles.add(staticFiles -> {
                    staticFiles.directory = "../frontend/dist";
                    staticFiles.hostedPath = "/";
                    // to say it's not packaged inside the .jar
                    staticFiles.location = Location.EXTERNAL;
                });
                // this is for our SPA specifically
                config.spaRoot.addFile("/", "../frontend/dist/index.html", Location.EXTERNAL);
            }

            config.jetty.modifyServer(server -> server.setStopTimeout(5_000)); // wait 5 seconds for existing requests
                                                                               // to finish

            config.http.maxRequestSize = 20 * 1000; // 20kb

            // create the routes here
            // see https://javalin.io/documentation#handler-groups
            // TODO dedicated routers?
            config.router.apiBuilder(() -> {
                path("/api", () -> {
                    get(ctx -> ctx.result("Hello world"));
                    path("/test", () -> {
                        get(ctx -> {
                            ctx.result("Hello world, but in test!");
                            log.info("User saw: Hello World in test");
                        });
                    });
                });
            });
        }).start(server_port);

        // for clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            ds.close();
        }));
    }
}
