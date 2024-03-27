package com.db1.exception.translator;

import com.db1.domain.Member;
import com.db1.repository.ex.MyDbException;
import com.db1.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static com.db1.connection.ConnectionConst.*;
import static org.springframework.jdbc.support.JdbcUtils.closeConnection;
import static org.springframework.jdbc.support.JdbcUtils.closeStatement;

public class ExTranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId");
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {
            try {
                log.info("memberId={}", memberId);
                repository.save(new Member(memberId, 0));
            } catch (MyDuplicateKeyException e) {
                log.info("키 중복~");
                String newMemberId = memberId + new Random().nextInt(10000);
                log.info("newMemberId={}", newMemberId);
                repository.save(new Member(newMemberId, 0));
            } catch (MyDbException e) {
                log.info("MyDbException", e);
                throw e;
            }
                
        }
    }


    @Slf4j
    @RequiredArgsConstructor
    static class Repository {

        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?, ?)";

            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                if (e.getErrorCode() == 23505) {
                    log.info("e.getErrorCode()={}", e.getErrorCode());
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e);
            } finally {
                closeStatement(pstmt);
                closeConnection(con);
            }
        }
    }
}
