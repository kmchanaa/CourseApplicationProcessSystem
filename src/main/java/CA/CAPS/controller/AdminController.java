package CA.CAPS.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import CA.CAPS.domain.Course;
import CA.CAPS.domain.Lecturer;
import CA.CAPS.service.AdminService;
import CA.CAPS.service.MailService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	AdminService adminService;
	
	@Autowired
	MailService mailservice;
	
	static int pageSize = 5;
	
	@GetMapping("/lecturer/add")
	public String addLecturer(Model model) {
		
		Lecturer lecturer = new Lecturer();
		model.addAttribute("lecturer", lecturer);
		
		return "admin/admin-lecturer-form";
	}
	
	@RequestMapping("/lecturer/save")
	public String saveLecturer(@ModelAttribute("lecturer") @Valid Lecturer lecturer, BindingResult bindingResult, Model model) {
		
		if (adminService.isUserNameExist(lecturer))
			bindingResult.rejectValue("userName", "error.userName", "Username already exists.");
		
		if(!lecturer.getUserName().isBlank() && !lecturer.getUserName().endsWith("@u.nus.edu"))
			bindingResult.rejectValue("userName", "error.userName", "Please use NUS email as Username.");
		
		if (bindingResult.hasErrors())
			return "admin/admin-lecturer-form";
		
		String message = "";
		if (lecturer.getId()==0) {
			String subject = "CAPS Account";
			String text = "Your CAPS Account has been created.\n"
					+ "Username: " + lecturer.getUserName() + "\n"
					+ "Password: " + lecturer.getPassword();

			boolean mailsent = mailservice.sendMail(lecturer.getUserName(), subject, text);
			
			if (mailsent)
				message = "You have created Lecturer " + lecturer + ". Acount details has been sent to the Lecturer.";
			else
				message = "You have created Lecturer " + lecturer + ".";			
		}
		else
			message = "You have updated Lecturer " + lecturer + ".";
		
		adminService.saveLecturer(lecturer);		
		model.addAttribute("message", message);
		
		return "forward:/admin/lecturer/list";
	}
	
	@GetMapping("/lecturer/edit/{id}")
	public String updateLecturer(Model model, @PathVariable("id") Integer id) {
		
		model.addAttribute("lecturer", adminService.findLecturerById(id));
		
		return "admin/admin-lecturer-form";
	}
	
	@GetMapping("/lecturer/delete/{id}")
	public String deleteLecturer(@PathVariable("id") Integer id,Model model) {
		
		Lecturer lecturer = adminService.findLecturerById(id);
		adminService.removeLecturerFromCourses(lecturer);
		adminService.deleteLecturer(lecturer);
		
		String message = "You have deleted Lectuerer " + lecturer;
		model.addAttribute("message", message);
		
		return "forward:/admin/lecturer/list";
	}
	
	@RequestMapping("/lecturer/list")
	public String listLecturers(@RequestParam("page") Optional<Integer> page, Model model) {
		
		int requestPage = page.orElse(1);
		Pageable pageable = PageRequest.of(requestPage-1, pageSize, Sort.by("userName"));
		Page<Lecturer> adminPage = adminService.findLecturerPaginated(pageable);
		model.addAttribute("adminPage", adminPage);
		
        int totalPages = adminPage.getTotalPages();        
        if (totalPages > 0 ) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        
		model.addAttribute("lecturers", adminService.listAllLecturers(pageable));
		
		return "admin/admin-view-lecturer";
	}
	
	@GetMapping("/course/add")
	public String addCourse(Model model) {
		
		Course course = new Course();
		model.addAttribute("course", course);
		model.addAttribute("lecturers", adminService.listAllLecturers());
		
		return "admin/admin-course-form";

	}
	
	@RequestMapping("/course/save")
	public String saveCourse(@ModelAttribute("course") @Valid Course course, BindingResult bindingResult, Model model) {
		
		model.addAttribute("lecturers", adminService.listAllLecturers());
		
		if (adminService.isCourseCodeExist(course))
			bindingResult.rejectValue("code", "error.code", "This course code already exists.");
		
		if (bindingResult.hasErrors())
			return "admin/admin-course-form";
		
		if (course.getLecturer()==null)
			course.setLecturer(null);
		else
			course.setLecturer(adminService.findByUserName(course.getLecturer().getUserName()));
		
		String message = "";
		if (course.getId()==0)
			message = "You have created Course " + course + ".";
		else
			message = "You have updated Course " + course + ".";
		
		adminService.saveCourse(course);
		model.addAttribute("message", message);
		
		return "forward:/admin/course/list";
	}
	
	@GetMapping("/course/edit/{id}")
	public String updateCourse(Model model, @PathVariable("id") Integer id) {
		
		model.addAttribute("lecturers", adminService.listAllLecturers());
		model.addAttribute("course", adminService.findCourseById(id));
		
		return "admin/admin-course-form";
	}
	
	@GetMapping("/course/delete/{id}")
	public String deleteCourse(@PathVariable("id") Integer id, Model model) {
		
		Course course = adminService.findCourseById(id);
		adminService.deleteCourse(course);
		
		String message = " You have deleted Course " + course;
		model.addAttribute("message", message);
		
		return "forward:/admin/course/list";
	}

	@RequestMapping("/course/list")
	public String listCourses(@RequestParam("page") Optional<Integer> page, Model model) {
		
		int requestPage = page.orElse(1);
		Pageable pageable = PageRequest.of(requestPage-1, pageSize, Sort.by("code"));
		Page<Course> adminPage = adminService.findCoursePaginated(pageable);
		model.addAttribute("adminPage", adminPage);
		
        int totalPages = adminPage.getTotalPages();        
        if (totalPages > 0 ) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        
		model.addAttribute("courses", adminService.listAllCourses(pageable));
		
		return "admin/admin-view-course";
	}
	
}