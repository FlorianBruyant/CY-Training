package org.cytraining.backend;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import java.sql.Connection;
import java.sql.SQLException;

import org.cytraining.backend.api.Response;
import org.cytraining.backend.model.tables.Account;
import org.cytraining.backend.utils.Dotenv;
import org.cytraining.backend.utils.Hikari;
import org.cytraining.backend.utils.Log;
import org.cytraining.backend.utils.PasswordHasher;
import org.cytraining.backend.utils.jOOQ;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;

import com.zaxxer.hikari.HikariDataSource;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {

    public static void main(String[] args) {
        // get the logger
        Logger log = Log.getLog();

        // setup jOOQ
        if (!jOOQ.isValid()) {
            return;
        }

        // get Hikari's data source
        HikariDataSource hds = Hikari.getHds();

        // Test connection
        try {
            Connection connection = hds.getConnection();
            log.info("Successfully connect to PostgreSQL server");
            connection.close();
        } catch (SQLException e) {
            Log.fatal("Failed to connecto to PostgreSQL server:", e);
            hds.close();
            return;
        }

        // setup admin account
        DSLContext jctx = jOOQ.getDsl();

        Result<Record> query = jctx.select().from(Account.ACCOUNT)
                .where(Account.ACCOUNT.EMAIL.eq("admin@cytraining.org")).fetch();
        if (query.size() == 0) {
            log.info("Admin account not found, creating one ...");
            // create an admin account
            jctx.insertInto(Account.ACCOUNT, Account.ACCOUNT.EMAIL, Account.ACCOUNT.FIRST_NAME,
                    Account.ACCOUNT.LAST_NAME, Account.ACCOUNT.PASSWORD)
                    .values("admin@cytraining.org", "admin", "admin",
                            PasswordHasher.hash(Dotenv.getAdminPass()))
                    .execute();
        }

        // Javalin app
        Javalin app = Javalin.create(config -> {
            // allow specific host on the api (CORS Access-Control-Allow-Origin), but ONLY
            // for dev mode
            // because in production, the backend and frontend are both served using the
            // same javalin server, so there are no cross origin requests
            if (Dotenv.isDevMode()) {
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        log.warn(
                                "Development mode enabled, allowing any host. This should not be used is production mode.");
                        it.anyHost();
                    });
                });
            }

            if (Dotenv.isDevMode()) {
                // for in depth automatic logging on each request
                // config.bundledPlugins.enableDevLogging();

                config.requestLogger.http((ctx, ms) -> {
                    // development logging here
                    Log.infoIp(ctx);
                });
            } else {
                config.showJavalinBanner = false;
                config.requestLogger.http((ctx, ms) -> {
                    // production logging
                    Log.infoIp(ctx);
                });
            }

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

            config.jetty.modifyServer(server -> server.setStopTimeout(5_000)); // wait 5 seconds for existing requests
                                                                               // to finish

            config.http.maxRequestSize = 20 * 1000; // 20kb

            // create the routes here
            // see https://javalin.io/documentation#handler-groups
            // TODO dedicated routers?
            config.router.apiBuilder(() -> {
                path("/api", () -> {
                    get(ctx -> ctx.json(Response.ok("Hello world!")));
                    path("/test", () -> {
                        get(ctx -> {
                            ctx.json(Response.ok("Hello world, but in test!"));
                            log.info("User saw: Hello World in test");
                        });
                    });
                });
            });
        }).start(Dotenv.getServerPort());

        // for clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            hds.close();
        }));
    }
}
