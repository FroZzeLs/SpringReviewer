package by.frozzel.springreviewer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.exception.BadRequestException;
import by.frozzel.springreviewer.exception.ResourceNotFoundException;
import by.frozzel.springreviewer.mapper.SubjectMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.ReviewRepository;
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
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private SubjectService subjectService;

    private Subject subject1;
    private Subject subject2;
    private Teacher teacher1;
    private SubjectCreateDto subjectCreateDto1;
    private SubjectDisplayDto subjectDisplayDto1;
    private SubjectDisplayDto subjectDisplayDto2;


    @BeforeEach
    void setUp() {
        subject1 = new Subject(1, "Math", new ArrayList<>());
        subject2 = new Subject(2, "Physics", new ArrayList<>());

        teacher1 = new Teacher(10, "Ivanov", "Ivan", "I.", new ArrayList<>(), new ArrayList<>());
        teacher1.getSubjects().add(subject1);
        subject1.getTeachers().add(teacher1);


        subjectCreateDto1 = new SubjectCreateDto("Math");

        subjectDisplayDto1 = new SubjectDisplayDto(1, "Math", List.of("Ivanov")); // Assume mapper gets names
        subjectDisplayDto2 = new SubjectDisplayDto(2, "Physics", Collections.emptyList());
    }

    @Test
    void createSubject_Success() {
        Subject newSubject = new Subject(0, "Math", new ArrayList<>());
        when(subjectMapper.toEntity(subjectCreateDto1)).thenReturn(newSubject);
        when(subjectRepository.save(newSubject)).thenReturn(subject1); // Returns subject with ID
        when(subjectMapper.toDto(subject1)).thenReturn(subjectDisplayDto1);

        SubjectDisplayDto result = subjectService.createSubject(subjectCreateDto1);

        assertNotNull(result);
        assertEquals("Math", result.getName());
        verify(subjectMapper).toEntity(subjectCreateDto1);
        verify(subjectRepository).save(newSubject);
        verify(subjectMapper).toDto(subject1);
    }

    @Test
    void getAllSubjects_Success() {
        when(subjectRepository.findAll()).thenReturn(List.of(subject1, subject2));
        when(subjectMapper.toDto(subject1)).thenReturn(subjectDisplayDto1);
        when(subjectMapper.toDto(subject2)).thenReturn(subjectDisplayDto2);

        List<SubjectDisplayDto> result = subjectService.getAllSubjects();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectRepository).findAll();
        verify(subjectMapper, times(2)).toDto(any(Subject.class));
    }

    @Test
    void getSubjectById_Success() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject1));
        when(subjectMapper.toDto(subject1)).thenReturn(subjectDisplayDto1);

        SubjectDisplayDto result = subjectService.getSubjectById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(subjectRepository).findById(1);
        verify(subjectMapper).toDto(subject1);
    }

    @Test
    void getSubjectById_NotFound() {
        when(subjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.getSubjectById(99));
        verify(subjectRepository).findById(99);
        verify(subjectMapper, never()).toDto(any());
    }

    @Test
    void getSubjectByName_Success() {
        when(subjectRepository.findByNameIgnoreCase("Math")).thenReturn(Optional.of(subject1));
        when(subjectMapper.toDto(subject1)).thenReturn(subjectDisplayDto1);

        SubjectDisplayDto result = subjectService.getSubjectByName("Math");

        assertNotNull(result);
        assertEquals("Math", result.getName());
        verify(subjectRepository).findByNameIgnoreCase("Math");
        verify(subjectMapper).toDto(subject1);
    }

    @Test
    void getSubjectByName_NotFound() {
        when(subjectRepository.findByNameIgnoreCase("Chemistry")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.getSubjectByName("Chemistry"));
        verify(subjectRepository).findByNameIgnoreCase("Chemistry");
        verify(subjectMapper, never()).toDto(any());
    }

    @Test
    void updateSubject_Success() {
        SubjectCreateDto updateDto = new SubjectCreateDto("Advanced Math");
        Subject updatedSubject = new Subject(1, "Advanced Math", subject1.getTeachers()); // Keep teachers
        SubjectDisplayDto updatedDisplayDto = new SubjectDisplayDto(1, "Advanced Math", List.of("Ivanov"));

        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject1));
        when(subjectRepository.save(any(Subject.class))).thenReturn(updatedSubject);
        when(subjectMapper.toDto(updatedSubject)).thenReturn(updatedDisplayDto);

        SubjectDisplayDto result = subjectService.updateSubject(1, updateDto);

        assertNotNull(result);
        assertEquals("Advanced Math", result.getName());
        verify(subjectRepository).findById(1);
        verify(subjectRepository).save(any(Subject.class)); // Verifies save is called
        verify(subjectMapper).toDto(updatedSubject);
    }

    @Test
    void updateSubject_NotFound() {
        SubjectCreateDto updateDto = new SubjectCreateDto("Advanced Math");
        when(subjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.updateSubject(99, updateDto));
        verify(subjectRepository).findById(99);
        verify(subjectRepository, never()).save(any());
        verify(subjectMapper, never()).toDto(any());
    }

    @Test
    void deleteSubject_Success() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject1));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher1);
        doNothing().when(reviewRepository).deleteBySubjectId(1);
        doNothing().when(subjectRepository).delete(subject1);

        subjectService.deleteSubject(1);

        verify(subjectRepository).findById(1);
        assertFalse(teacher1.getSubjects().contains(subject1)); // Check if removed from teacher
        assertTrue(subject1.getTeachers().isEmpty()); // Check if teacher list is cleared in subject
        verify(teacherRepository).save(teacher1); // Verify teacher is saved after modification
        verify(reviewRepository).deleteBySubjectId(1);
        verify(subjectRepository).delete(subject1);
    }

    @Test
    void deleteSubject_Success_NoTeachers() {
        subject1.getTeachers().clear(); // Remove teacher association
        teacher1.getSubjects().clear(); // Remove subject association

        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject1));
        doNothing().when(reviewRepository).deleteBySubjectId(1);
        doNothing().when(subjectRepository).delete(subject1);

        subjectService.deleteSubject(1);

        verify(subjectRepository).findById(1);
        verify(teacherRepository, never()).save(any(Teacher.class)); // Teacher repo not called
        verify(reviewRepository).deleteBySubjectId(1);
        verify(subjectRepository).delete(subject1);
    }


    @Test
    void deleteSubject_NotFound() {
        when(subjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.deleteSubject(99));
        verify(subjectRepository).findById(99);
        verify(teacherRepository, never()).save(any());
        verify(reviewRepository, never()).deleteBySubjectId(anyInt());
        verify(subjectRepository, never()).delete(any());
    }

    @Test
    void deleteSubject_BadRequest_NegativeId() {
        assertThrows(BadRequestException.class, () -> subjectService.deleteSubject(-1));
        verifyNoInteractions(subjectRepository, teacherRepository, reviewRepository, subjectMapper);
    }

    @Test
    void deleteSubject_BadRequest_ZeroId() {
        assertThrows(BadRequestException.class, () -> subjectService.deleteSubject(0));
        verifyNoInteractions(subjectRepository, teacherRepository, reviewRepository, subjectMapper);
    }
}