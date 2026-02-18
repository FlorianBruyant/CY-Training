package org.cytraining.backend;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;

import io.github.cdimascio.dotenv.Dotenv;

public class jOOQSetup {
    public static Exception setup() {
        // TODO check if already generated
        // TODO generate every time in dev mode
        // TODO use global env

        // TODO do it in a singleton
        Dotenv dotenv = Dotenv.load();

        String db_host = dotenv.get("DATABASE_HOST");
        String db_port = dotenv.get("DATABASE_PORT");
        String db_name = dotenv.get("DATABASE_NAME");
        String db_user = dotenv.get("DATABASE_USER");
        String db_pass = dotenv.get("DATABASE_PASS");

        String db_url = "jdbc:postgresql://" + db_host + ":" + db_port + "/" + db_name;

        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(db_url)
                        .withUser(db_user)
                        .withPassword(db_pass))
                .withGenerator(new Generator()
                        .withName("org.jooq.codegen.DefaultGenerator")
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("public")
                                .withExcludes("flyway_schema_history"))
                        .withGenerate(new Generate()
                                .withPojos(true)
                                .withDaos(true)
                                .withRecords(true))
                        .withTarget(new Target()
                                .withPackageName("org.cytraining.backend")
                                .withDirectory("target/generated-sources/jooq")));

        try {
            GenerationTool.generate(configuration);
            return null;
        } catch (Exception e) {
            return e;
        }
    }
}
