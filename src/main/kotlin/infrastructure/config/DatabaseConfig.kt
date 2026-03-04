package com.ecommerce.infrastructure.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    fun initDatabase(
        jdbcUrl: String,
        username: String,
        password: String,
        driverClassName: String = "org.postgresql.Driver"
    ): Database {
        val dataSource = HikariDataSource(HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            this.driverClassName = driverClassName
            maximumPoolSize = 10
            minimumIdle = 5
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
        })

        // Run Flyway migrations
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()

        return Database.connect(dataSource)
    }
}