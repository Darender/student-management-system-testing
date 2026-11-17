package tests.com.sms;

import com.sms.dao.DataAccessException;
import com.sms.dao.JdbcStudentDao;
import com.sms.db.Database;
import com.sms.db.Dialect;
import com.sms.model.Student;
import com.sms.model.Department;
import com.sms.model.Gender;
import com.sms.service.StudentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StudentDeletionTest {

    @TempDir
    Path tempDir;

    private StudentService service;

    @BeforeEach
    void setUp() {

        Database database = Database.sqlite(
            tempDir.resolve("delete-test.db")
        );

        database.createSchema();

        JdbcStudentDao dao = new JdbcStudentDao(database);

        service = new StudentService(dao);
    }

    // Estudiante de prueba
    private Student createStudent() {

        return Student.builder()
            .rollNo("23CSE101")
            .name("Juan Perez")
            .email("juan@test.com")
            .phone("1234567890")
            .department(Department.CSE)
            .semester(3)
            .cgpa(8.5)
            .gender(Gender.MALE)
            .dateOfBirth(LocalDate.of(2000, 1, 1))
            .address("Mexicali")
            .build();
    }

    // DEL-01: Eliminación exitosa
    @Test
    void eliminarEstudianteExistente() throws Exception {

        Student saved = service.add(createStudent());

        int id = saved.getId();

        boolean result = service.delete(id);

        assertTrue(result);
        assertFalse(service.findById(id).isPresent());
        assertEquals(0, service.count());
    }

    // DEL-04: Intentar eliminar un estudiante inexistente
    @Test
    void rechazarEliminacionEstudianteInexistente() {

        int idInexistente = 99999;

        boolean result = service.delete(idInexistente);

        assertFalse(result);
        assertEquals(0, service.count());
    }

    // DEL-05: Fallo de conexión con SQLite
    @Test
    void manejarFalloConexionBaseDatos() throws Exception {

        Path invalidPath = tempDir.resolve("database-directory");
        Files.createDirectory(invalidPath);

        Database brokenDatabase = new Database(
            "jdbc:sqlite:" + invalidPath.toAbsolutePath(),
            "",
            "",
            Dialect.SQLITE
        );

        StudentService brokenService = new StudentService(
            new JdbcStudentDao(brokenDatabase)
        );

        assertThrows(
            DataAccessException.class,
            () -> brokenService.delete(1)
        );
    }
}
