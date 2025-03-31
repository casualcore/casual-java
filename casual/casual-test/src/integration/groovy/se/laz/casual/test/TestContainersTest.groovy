/*
 * Copyright (c) 2024 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.sql.ResultSet
import java.sql.Statement

@Ignore
@Testcontainers
class TestContainersTest extends Specification
{
    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer( DockerImageName.parse( "postgres" ))
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret")

    def "waits until postgres accepts jdbc connections"() {

        given: "a jdbc connection"
        HikariConfig hikariConfig = new HikariConfig()
        hikariConfig.setJdbcUrl(postgreSQLContainer.jdbcUrl)
        hikariConfig.setUsername("foo")
        hikariConfig.setPassword("secret")
        HikariDataSource ds = new HikariDataSource(hikariConfig)

        when: "querying the database"
        Statement statement = ds.getConnection().createStatement()
        statement.execute("SELECT 1")
        ResultSet resultSet = statement.getResultSet()
        resultSet.next()

        then: "result is returned"
        int resultSetInt = resultSet.getInt(1)
        resultSetInt == 1

        cleanup:
        ds.close()
    }
}
