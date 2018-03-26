package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.UserService;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.config.CloudinaryConfig;
import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	MessageRepository messageRepository;

	@Autowired
	private UserService userService;

	@Autowired
	CloudinaryConfig cloudc;

	@Autowired
	UserRepository userRepository;

	@RequestMapping("/")
	public String listMessages(Model model) {
		model.addAttribute("users", userRepository.findAll());
		return "secure";
	}

	@GetMapping("/add")
	public String courseForm(Model model) {
		model.addAttribute("message", new Message());
		return "messageform";
	}

	@PostMapping("/add")
	public String processMessage(@Valid @ModelAttribute("message") Message message,
			@Valid @ModelAttribute("user") User user,
			@RequestParam("file") MultipartFile file, BindingResult result) {
		if (result.hasErrors()) {
			return "messageform";
		}

		if (file.isEmpty()) {
			return "redirect:/add";
		}

		try {
			Map<?, ?> uploadResult = cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
			message.setImage(uploadResult.get("url").toString());
			messageRepository.save(message);
		} catch (IOException e) {
			e.printStackTrace();
			return "redirect:/add";
		}

		
		Set<Message> messages = new HashSet<Message>();
		
		message.setSentBy(user.getUsername());
		messages.add(message);
		
		user.setMessages(messages);
		
		userRepository.save(user);
		
		return "redirect:/";
	}

	@RequestMapping("/detail/{id}")
	public String showCourse(@PathVariable("id") long id, Model model) {
		model.addAttribute("message", messageRepository.findOne(id));
		return "show";
	}

	@RequestMapping("/update/{id}")
	public String updateCourse(@PathVariable("id") long id, Model model) {
		model.addAttribute("message", messageRepository.findOne(id));
		return "messageform";
	}

	@RequestMapping("/delete/{id}")
	public String delCourse(@PathVariable("id") long id) {
		messageRepository.delete(id);
		return "redirect:/";
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String showRegistrationPage(Model model) {
		model.addAttribute("user", new User());
		return "registration";
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
		model.addAttribute("user", user);
		if (result.hasErrors()) {
			return "registration";
		} else {
			userService.saveUser(user);
			model.addAttribute("message", "User Account Successfully Created");
		}

		return "login";
	}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

}