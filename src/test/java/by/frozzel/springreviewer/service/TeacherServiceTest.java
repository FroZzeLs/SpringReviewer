package by.frozzel.springreviewer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.exception.BadRequestException;
import by.frozzel.springreviewer.exception.ResourceNotFoundException;
import by.frozzel.springreviewer.mapper.TeacherMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher teacher1;
    private Teacher teacher2;
    private Subject subject1;
    private Subject subject2;
    private TeacherCreateDto teacherCreateDto1;
    private TeacherDisplayDto teacherDisplayDto1;
    private TeacherDisplayDto teacherDisplayDto2;

    @BeforeEach
    void setUp() {
        subject1 = new Subject(10, "Math", new ArrayList<>());
        subject2 = new Subject(20, "Physics", new ArrayList<>());

        teacher1 = new Teacher(1, "Ivanov", "Ivan", "Ivanovich", new ArrayList<>(), new ArrayList<>());
        teacher1.getSubjects().add(subject1);

        teacher2 = new Teacher(2, "Petrov", "Petr", null, new ArrayList<>(), new ArrayList<>());

        teacherCreateDto1 = new TeacherCreateDto();
        teacherCreateDto1.setSurname("Ivanov");
        teacherCreateDto1.setName("Ivan");
        teacherCreateDto1.setPatronym("Ivanovich");

        teacherDisplayDto1 = TeacherDisplayDto.builder()
                .id(1)
                .surname("Ivanov")
                .name("Ivan")
                .patronym("Ivanovich")
                .subjects(List.of("Math"))
                .build();

        teacherDisplayDto2 = TeacherDisplayDto.builder()
                .id(2)
                .surname("Petrov")
                .name("Petr")
                .subjects(Collections.emptyList())
                .build();
    }

    @Test
    void getAllTeachers_Success() {
        when(teacherRepository.findAll()).thenReturn(List.of(teacher1, teacher2));
        when(teacherMapper.toDto(teacher1)).thenReturn(teacherDisplayDto1);
        when(teacherMapper.toDto(teacher2)).thenReturn(teacherDisplayDto2);

        List<TeacherDisplayDto> result = teacherService.getAllTeachers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(teacherRepository).findAll();
        verify(teacherMapper, times(2)).toDto(any(Teacher.class));
    }

    @Test
    void getTeacherById_Success() {
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(teacherMapper.toDto(teacher1)).thenReturn(teacherDisplayDto1);

        TeacherDisplayDto result = teacherService.getTeacherById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(teacherRepository).findById(1);
        verify(teacherMapper).toDto(teacher1);
    }

    @Test
    void getTeacherById_NotFound() {
        when(teacherRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherById(99));
        verify(teacherRepository).findById(99);
        verify(teacherMapper, never()).toDto(any());
    }

    @Test
    void createTeacher_Success() {
        Teacher newTeacher = new Teacher(0, "Ivanov", "Ivan", "Ivanovich", new ArrayList<>(), new ArrayList<>());
        Teacher savedTeacher = new Teacher(1, "Ivanov", "Ivan", "Ivanovich", new ArrayList<>(), new ArrayList<>());
        when(teacherMapper.toEntity(teacherCreateDto1)).thenReturn(newTeacher);
        when(teacherRepository.save(newTeacher)).thenReturn(savedTeacher);
        when(teacherMapper.toDto(savedTeacher)).thenReturn(teacherDisplayDto1);

        TeacherDisplayDto result = teacherService.createTeacher(teacherCreateDto1);

        assertNotNull(result);
        assertEquals("Ivanov", result.getSurname());
        verify(teacherMapper).toEntity(teacherCreateDto1);
        verify(teacherRepository).save(newTeacher);
        verify(teacherMapper).toDto(savedTeacher);
    }

    @Test
    void createTeachersBulk_Success() {
        TeacherCreateDto createDto2 = new TeacherCreateDto();
        createDto2.setSurname("Petrov");
        createDto2.setName("Petr");
        List<TeacherCreateDto> createDtos = List.of(teacherCreateDto1, createDto2);

        Teacher newTeacher1 = new Teacher(0, "Ivanov", "Ivan", "Ivanovich", new ArrayList<>(), new ArrayList<>());
        Teacher newTeacher2 = new Teacher(0, "Petrov", "Petr", null, new ArrayList<>(), new ArrayList<>());

        when(teacherMapper.toEntity(teacherCreateDto1)).thenReturn(newTeacher1);
        when(teacherMapper.toEntity(createDto2)).thenReturn(newTeacher2);

        List<Teacher> teachersToSave = List.of(newTeacher1, newTeacher2);
        List<Teacher> savedTeachers = List.of(teacher1, teacher2);

        when(teacherRepository.saveAll(teachersToSave)).thenReturn(savedTeachers);
        when(teacherMapper.toDto(teacher1)).thenReturn(teacherDisplayDto1);
        when(teacherMapper.toDto(teacher2)).thenReturn(teacherDisplayDto2);

        List<TeacherDisplayDto> results = teacherService.createTeachersBulk(createDtos);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(teacherMapper, times(2)).toEntity(any(TeacherCreateDto.class));
        verify(teacherRepository).saveAll(teachersToSave);
        verify(teacherMapper, times(2)).toDto(any(Teacher.class));
    }


    @Test
    void updateTeacher_Success() {
        TeacherCreateDto updateDto = new TeacherCreateDto();
        updateDto.setSurname("Sidorov");
        updateDto.setName("Sidor");
        updateDto.setPatronym("Sidorovich");

        Teacher updatedTeacher = new Teacher(1, "Sidorov", "Sidor", "Sidorovich", new ArrayList<>(), teacher1.getSubjects()); // Keep subjects

        TeacherDisplayDto updatedDisplayDto = TeacherDisplayDto.builder()
                .id(1)
                .surname("Sidorov")
                .name("Sidor")
                .patronym("Sidorovich")
                .subjects(List.of("Math")) // Assume mapper handles this
                .build();

        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(updatedTeacher);
        when(teacherMapper.toDto(updatedTeacher)).thenReturn(updatedDisplayDto);

        TeacherDisplayDto result = teacherService.updateTeacher(1, updateDto);

        assertNotNull(result);
        assertEquals("Sidorov", result.getSurname());
        assertEquals("Sidor", result.getName());
        assertEquals("Sidorovich", result.getPatronym());
        verify(teacherRepository).findById(1);
        verify(teacherRepository).save(any(Teacher.class));
        verify(teacherMapper).toDto(updatedTeacher);
    }

    @Test
    void updateTeacher_NotFound() {
        TeacherCreateDto updateDto = new TeacherCreateDto();
        when(teacherRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.updateTeacher(99, updateDto));
        verify(teacherRepository).findById(99);
        verify(teacherRepository, never()).save(any());
        verify(teacherMapper, never()).toDto(any());
    }

    @Test
    void deleteTeacher_Success() {
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        doNothing().when(teacherRepository).deleteById(1);

        teacherService.deleteTeacher(1);

        verify(teacherRepository).findById(1);
        verify(teacherRepository).deleteById(1);
    }

    @Test
    void deleteTeacher_NotFound() {
        when(teacherRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.deleteTeacher(99));
        verify(teacherRepository).findById(99);
        verify(teacherRepository, never()).deleteById(anyInt());
    }

    @Test
    void getTeacherByFullName_Success() {
        when(teacherRepository.findBySurnameAndNameIgnoreCase("Ivanov", "Ivan")).thenReturn(Optional.of(teacher1));
        when(teacherMapper.toDto(teacher1)).thenReturn(teacherDisplayDto1);

        TeacherDisplayDto result = teacherService.getTeacherByFullName("Ivanov", "Ivan");

        assertNotNull(result);
        assertEquals("Ivanov", result.getSurname());
        assertEquals("Ivan", result.getName());
        verify(teacherRepository).findBySurnameAndNameIgnoreCase("Ivanov", "Ivan");
        verify(teacherMapper).toDto(teacher1);
    }

    @Test
    void getTeacherByFullName_NotFound() {
        when(teacherRepository.findBySurnameAndNameIgnoreCase("Non", "Existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherByFullName("Non", "Existent"));
        verify(teacherRepository).findBySurnameAndNameIgnoreCase("Non", "Existent");
        verify(teacherMapper, never()).toDto(any());
    }

    @Test
    void assignSubjectToTeacher_Success_NewAssignment() {
        teacher1.getSubjects().clear(); // Start fresh
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(subjectRepository.findById(20)).thenReturn(Optional.of(subject2));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher1);

        teacherService.assignSubjectToTeacher(1, 20);

        assertTrue(teacher1.getSubjects().contains(subject2));
        verify(teacherRepository).findById(1);
        verify(subjectRepository).findById(20);
        verify(teacherRepository).save(teacher1);
    }

    @Test
    void assignSubjectToTeacher_Success_AlreadyAssigned() {
        // Setup ensures subject1 is already assigned
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(subjectRepository.findById(10)).thenReturn(Optional.of(subject1));

        teacherService.assignSubjectToTeacher(1, 10);

        verify(teacherRepository).findById(1);
        verify(subjectRepository).findById(10);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void assignSubjectToTeacher_TeacherNotFound() {
        when(teacherRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.assignSubjectToTeacher(99, 10));
        verify(teacherRepository).findById(99);
        verify(subjectRepository, never()).findById(anyInt());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void assignSubjectToTeacher_SubjectNotFound() {
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(subjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.assignSubjectToTeacher(1, 99));
        verify(teacherRepository).findById(1);
        verify(subjectRepository).findById(99);
        verify(teacherRepository, never()).save(any());
    }


    @Test
    void removeSubjectFromTeacher_Success() {
        // Setup ensures subject1 is assigned
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(subjectRepository.findById(10)).thenReturn(Optional.of(subject1));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher1);

        teacherService.removeSubjectFromTeacher(1, 10);

        assertFalse(teacher1.getSubjects().contains(subject1));
        verify(teacherRepository).findById(1);
        verify(subjectRepository).findById(10);
        verify(teacherRepository).save(teacher1);
    }

    @Test
    void removeSubjectFromTeacher_SubjectNotAssigned() {
        // Setup ensures subject2 is NOT assigned
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(subjectRepository.findById(20)).thenReturn(Optional.of(subject2));

        assertThrows(BadRequestException.class, () -> teacherService.removeSubjectFromTeacher(1, 20));

        verify(teacherRepository).findById(1);
        verify(subjectRepository).findById(20);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void removeSubjectFromTeacher_TeacherNotFound() {
        when(teacherRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.removeSubjectFromTeacher(99, 10));
        verify(teacherRepository).findById(99);
        verify(subjectRepository, never()).findById(anyInt());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void removeSubjectFromTeacher_SubjectNotFound() {
        when(teacherRepository.findById(1)).thenReturn(Optional.of(teacher1));
        when(subjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.removeSubjectFromTeacher(1, 99));
        verify(teacherRepository).findById(1);
        verify(subjectRepository).findById(99);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void getTeachersBySubjectName_Success() {
        when(teacherRepository.findTeachersBySubjectName("Math")).thenReturn(List.of(teacher1));
        when(teacherMapper.toDto(teacher1)).thenReturn(teacherDisplayDto1);

        List<TeacherDisplayDto> result = teacherService.getTeachersBySubjectName("Math");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ivanov", result.get(0).getSurname());
        verify(teacherRepository).findTeachersBySubjectName("Math");
        verify(teacherMapper).toDto(teacher1);
    }

    @Test
    void getTeachersBySubjectName_NotFound() {
        when(teacherRepository.findTeachersBySubjectName("Chemistry")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeachersBySubjectName("Chemistry"));
        verify(teacherRepository).findTeachersBySubjectName("Chemistry");
        verify(teacherMapper, never()).toDto(any());
    }
}