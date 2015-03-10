package demo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import demo.domain.Blog;
import demo.repositories.BlogRepository;

@Controller
public class BlogController {
	@Autowired
	BlogRepository blogRepository;

	@RequestMapping("/")
	String home(Model model) {
		String blogger = "hao";
		model.addAttribute("blogger", blogger);
		return "welcome";
	}

	@RequestMapping("/postBlog")
	String postBlog() {
		return "postBlog";
	}

	@RequestMapping(value = "/postBlog.do", method = RequestMethod.POST)
	String postBlogDo(@ModelAttribute("blog") Blog blog, Model model)
			throws Exception {
		blogRepository.save(blog);
		Iterable<Blog> blogs = blogRepository.findAll();
		model.addAttribute("blogs", blogs);
		return "listAllBlog";
	}
}
