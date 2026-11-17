package tests.com.sms;

import com.sms.db.Database;
import com.sms.dao.JdbcStudentDao;
import com.sms.service.StudentService;
import com.sms.service.ValidationException;
import com.sms.model.Student;
import com.sms.model.Department;
import com.sms.model.Gender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StudentRegistrationTest {

    @TempDir
    Path tempDir;

    private StudentService service;

    @BeforeEach
    void setUp() {
        Database database = Database.sqlite(
            tempDir.resolve("test.db")
        );

        database.createSchema();

        JdbcStudentDao dao = new JdbcStudentDao(database);

        service = new StudentService(dao);
    }

    // Datos válidos para reutilizar en las pruebas
    private Student validStudent() {
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

    // UT-01: Registro exitoso
    @Test
    void registrarEstudianteValido() throws Exception {

        Student saved = service.add(validStudent());

        assertTrue(saved.getId() > 0);
        assertEquals(1, service.count());
        assertTrue(service.findById(saved.getId()).isPresent());
    }

    // UT-02: Nombre vacío
    @Test
    void rechazarNombreVacio() {

        Student student = validStudent()
            .toBuilder()
            .name("")
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("name"));
    }

    // UT-03: Correo inválido
    @Test
    void rechazarCorreoInvalido() {

        Student student = validStudent()
            .toBuilder()
            .email("juan.com")
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("email"));
    }

    // UT-04: Teléfono inválido
    @Test
    void rechazarTelefonoInvalido() {

        Student student = validStudent()
            .toBuilder()
            .phone("12345")
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("phone"));
    }

    // UT-05: CGPA fuera del rango
    @Test
    void rechazarCgpaInvalido() {

        Student student = validStudent()
            .toBuilder()
            .cgpa(10.5)
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("cgpa"));
    }

    // UT-06: Matrícula con formato inválido
    @Test
    void rechazarMatriculaInvalida() {

        Student student = validStudent()
            .toBuilder()
            .rollNo("###")
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("rollNo"));
    }

    // UT-07: Semestre fuera del rango
    @Test
    void rechazarSemestreInvalido() {

        Student student = validStudent()
            .toBuilder()
            .semester(9)
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("semester"));
    }

    // UT-08: Departamento no seleccionado
    @Test
    void rechazarDepartamentoVacio() {

        Student student = validStudent()
            .toBuilder()
            .department(null)
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("department"));
    }

    // UT-09: Fecha de nacimiento vacía
    @Test
    void rechazarFechaNacimientoVacia() {

        Student student = validStudent()
            .toBuilder()
            .dateOfBirth(null)
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(student)
        );

        assertTrue(error.getErrors().containsKey("dateOfBirth"));
    }

    // UT-10: Matrícula duplicada
    @Test
    void rechazarMatriculaDuplicada() throws Exception {

        service.add(validStudent());

        Student duplicate = validStudent()
            .toBuilder()
            .email("otro@test.com")
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(duplicate)
        );

        assertTrue(error.getErrors().containsKey("rollNo"));
    }

    // UT-11: Correo duplicado
    @Test
    void rechazarCorreoDuplicado() throws Exception {

        service.add(validStudent());

        Student duplicate = validStudent()
            .toBuilder()
            .rollNo("23CSE102")
            .build();

        ValidationException error = assertThrows(
            ValidationException.class,
            () -> service.add(duplicate)
        );

        assertTrue(error.getErrors().containsKey("email"));
    }
}
