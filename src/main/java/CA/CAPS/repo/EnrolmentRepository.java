package CA.CAPS.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import CA.CAPS.domain.Course;
import CA.CAPS.domain.Enrolment;
import CA.CAPS.domain.Student;
import CA.CAPS.domain.enrolmentUPK;

public interface EnrolmentRepository extends JpaRepository<Enrolment, enrolmentUPK> {
	
	public List<Enrolment> findByStudent(Student student);
		
	@Query("Select e.course from Enrolment e where e.student = :student")
	public List<Course> findCourseByStudent(Student student);

	@Query("SELECT e.student FROM Enrolment e WHERE e.course.id = :id")
	public List<Student> findStudentsbyCourseId(@Param("id") int id);
	
	@Query("SELECT e.marks FROM Enrolment e WHERE e.course.id = :id")
	public List<Integer> findMarksByCourseId(@Param("id") int id);
	
	@Query("SELECT e FROM Enrolment e WHERE e.student.id = :id")
	public List<Enrolment> findEnrolByStudentId(@Param("id") int id);
	
	@Query("SELECT e.marks FROM Enrolment e WHERE e.student.id = :id")
	public List<Integer> findMarksByStudentId(@Param("id") int id);
	
	@Query("SELECT e FROM Enrolment e WHERE e.student = :student AND e.course = :course")
	public Enrolment findEnrolByStudentCourse(Student student, Course course);
	
	@Query("SELECT e.marks FROM Enrolment e WHERE e.course = :course")
	public Page<Integer> findByMarks(Pageable pageable, @Param("course") Course course);
}
