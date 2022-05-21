package org.loose.fis.sre.services;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.loose.fis.sre.exceptions.IncorrectCardDataException;
import org.loose.fis.sre.exceptions.UsernameAlreadyExistsException;
import org.loose.fis.sre.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testfx.assertions.api.Assertions.assertThat;

class UserServiceTest {

    public static final String ADMIN = "admin";

    @BeforeAll
    static void beforeAll() {
        System.out.println("Before Class");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("After Class");
    }

    @BeforeEach
    void setUp() throws Exception {
        FileSystemService.APPLICATION_FOLDER = ".registration-example";
        FileUtils.cleanDirectory(FileSystemService.getApplicationHomeFolder().toFile());
        UserService.initDatabase();
    }

    @AfterEach
    void tearDown() {
        System.out.println("After each");
    }


    @Test
    @DisplayName("Database is initialized, and there are no users")
    void testDatabaseIsInitializedAndNoUserIsPersisted() {
        assertThat(UserService.getAllUsers()).isNotNull();
        assertThat(UserService.getAllUsers()).isEmpty();
    }

    @Test
    @DisplayName("User is successfully persisted to Database")
    void testUserIsAddedToDatabase() throws UsernameAlreadyExistsException {
        UserService.addUser(ADMIN, ADMIN, ADMIN);
        assertThat(UserService.getAllUsers()).isNotEmpty();
        assertThat(UserService.getAllUsers()).size().isEqualTo(1);
        User user = UserService.getAllUsers().get(0);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(ADMIN);
        assertThat(user.getPassword()).isEqualTo(UserService.encodePassword(ADMIN, ADMIN));
        assertThat(user.getRole()).isEqualTo(ADMIN);
    }

    @Test
    @DisplayName("User can not be added twice")
    void testUserCanNotBeAddedTwice() {
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            UserService.addUser(ADMIN, ADMIN, ADMIN);
            UserService.addUser(ADMIN, ADMIN, ADMIN);
        });
    }

    @Test
    @DisplayName("Membership is updated correctly if the membership is not paid")
    void testMembershipUpdateNotPaid() throws UsernameAlreadyExistsException {
        UserService.addUser(ADMIN, ADMIN, ADMIN);
        UserService.updateMembership(ADMIN, 0, "membership");
        User user = UserService.getAllUsers().get(0);
        assertThat(user.getMemberships().get(0)).isEqualTo("membership");
        assertThat(user.getDaysLeft()).isEqualTo(0);
    }

    @Test
    @DisplayName("Membership is updated correctly if the membership is paid")
    void testMembershipUpdatePaid() throws UsernameAlreadyExistsException {
        UserService.addUser(ADMIN, ADMIN, ADMIN);
        UserService.updateMembership(ADMIN, 0, "membership");
        UserService.updateMembership(ADMIN, 5, "membership");
        User user = UserService.getAllUsers().get(0);
        assertThat(user.getMemberships()).isEmpty();
        assertThat(user.getDaysLeft()).isEqualTo(5);
    }

    @Test
    @DisplayName("The card validation is working")
    void testCardIsValid() {
        assertThrows(IncorrectCardDataException.class, () -> {
            UserService.checkValidCard("1111222", "a", "01/24", "174");
        });
        assertThrows(IncorrectCardDataException.class, () -> {
            UserService.checkValidCard("1111222233334444", "", "01/24", "174");
        });
        assertThrows(IncorrectCardDataException.class, () -> {
            UserService.checkValidCard("1111222233334444", "a", "1/24", "174");
        });
        assertThrows(IncorrectCardDataException.class, () -> {
            UserService.checkValidCard("1111222233334444", "a", "01/24", "aaa");
        });
    }
}