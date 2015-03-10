## 个人网站搭建系列: Spring MVC, JPA, H2数据库
### 基础准备
#### 开发环境及工具
* Jdk 1.7及以上
* Maven 3.2及以上 [下载地址](http://maven.apache.org/download.cgi)
* Spring Boot CLI [下载地址](http://repo.spring.io/release/org/springframework/boot/spring-boot-cli/)

#### 参考站点
* 本文源码 <https://github.com/imlinhao/website-mvc-jpa-h2.git>
* Spring Boot Reference Guide <http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#cli-init>
* Spring Guides <http://spring.io/guides>

#### 目标与方法描述
本文的目标是快速搭建一个简易博客，该博客可以完成基本的博客撰写与浏览。

本文将使用Spring Boot CLI来生成项目的基础结构。然后以Blog为例，采用JPA方法，进行数据存取。H2数据库(一个内存数据库)的使用，是为了简化数据库开发部署。Spring MVC中的模板技术，本文选择Thymeleaf。

### Spring Boot CLI初始化项目
Spring Initializer <https://start.spring.io/>，提供了生成初始项目的简化方法。而使用Spring Boot CLI的`init`命令，则直接可以使用命令行生成初始项目。

	spring init --dependencies=web,data-jpa,h2,thymeleaf website-mvc-jpa-h2

此时，项目便有了基本的项目目录结构与所需的基本依赖。对于该服务所提供的依赖管理能力，可以通过`spring init --list`来查看。

### Spring JPA
#### 定义JPA实体
首先，对于本文的简单博客，我们只需一个数据库对象Blog，该对象包含两个属性：title(标题)与content(内容)，此外为了能唯一标识一篇博客，另外在添加id属性。其代码如下：

`src/main/java/domain/Blog.java`

	package demo.domain;
	import javax.persistence.Entity;
	import javax.persistence.GeneratedValue;
	import javax.persistence.GenerationType;
	import javax.persistence.Id;
	
	@Entity
	public class Blog {
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;
		private String title;
		private String content;
	
		Blog() {
		}
	
		public Blog(String title, String content) {
			this.title = title;
			this.content = content;
		}
	}

该代码中，`缺省构造函数`的存在，是提供给JPA使用的。`Blog`类被标注为`@Entity`，表示它是一个JPA实体。`Blog`的`id`属性被标注为`@Id`，JPA就认为该属性为对象的ID。`id`属性还被标注为`@GeneratedValue`表示这个ID需要被自动生成。**注**:为了行文简洁，该代码省略get、set方法。

#### 建立repository接口
Spring Data JPA专注于使用JPA来对关系型数据库进行数据存取。它的一个非常好的特性就是，在运行时，可以通过repository接口，自动创建repository实现。

在此，我们根据`Blog`实体，来建立一个repository接口：`BlogRepository`。代码如下：

`src/main/java/demo/repositories/BlogRepository.java`

	package demo.repositories;
	import java.util.List;
	import org.springframework.data.repository.CrudRepository;
	import demo.domain.Blog;

	public interface BlogRepository extends CrudRepository<Blog, Long> {
		List<Blog> findByTitle(String title);
	}

`BlogRepository`扩展了`CrudRepository`接口。JPA实体以及它的ID的类型，`Blog`和`Long`，作为泛型参数传递给`CrudRepository`。通过继承`CrudRepository`，`BlogRepository`就自动有了`Blog`持久化的若干方法，包括保存、删除、查询`Blog`实体。

Spring Data JPA还提供了其他一些查询方法，只需要在接口中定义一下方法即可。`BlogRepository`中的`findByTitle()`便是一个案例。

### Spring MVC
使用Spring的方法来建立web站点，HTTP请求将会由controller来处理。可以简单地添加一个`@Controller`标注就可以定义一个controller。

`src/main/java/demo/web/BlogController.java`

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

在我们的`BlogController`中，`@RequestMapping`可以将请求路径与函数进行绑定。函数方法中返回的则是`View`的名字，如`home()`方法中，返回的就是名为`welcome`的`View`。`View`的作用是为了提供HTML内容。而`home()`方法中，使用的`Model`，则是存储了一些我们所需要的变量，最终这些变量将会提供给view模板使用。

对于view的提供，在此我们使用Thymeleaf实现。Thymeleaf解析如下的`welcome.html`模板，然后通过`th:text`表达式来获取controller里面设置的参数`${blogger}`。

`src/main/resources/templates/welcome.html`

	<!DOCTYPE HTML>
	<html xmlns:th="http://www.thymeleaf.org">
	<head>
	    <title>Getting Started: Serving Web Content</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	</head>
	<body>
	    <h1 th:text="'欢迎来到' + ${blogger} + '的博客!'" />
	</body>
	</html>

类似于`home()`方法，`BlogController`中的`postBlog()`打开一个postBlog view，该view包含两个文本框，进行博客编辑，然后将博客内容提交到`/postBlog.do`。

`src/main/resources/templates/postBlog.html`

	<!DOCTYPE HTML>
	<html xmlns:th="http://www.thymeleaf.org">
	<head>
	<title>发布博客</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	</head>
	<body>
	<form method="post" action="postBlog.do">
		标题: <input id="id_title" type="text"   name="title"/>
		内容: <textarea id="id_content" name="content"></textarea>
		<input type="submit" value="发表博文" />
	</form>
	</body>
	</html>

而`postBlogDo()`则接收，POST过来的Blog，通过Spring DATA JPA自动创建的`blogRepository`，将其保存到数据库中。保存结束之后，我们通过`blogRepository`的`findAll()`方法，将数据库中的所有blog都取出来，然后加入`Model`中，供`View`使用。view中，通过Thymeleaf的`th:each`来取得blogs中的blog。

`src/main/resources/templates/listAllBlog.html`

	<!DOCTYPE HTML>
	<html xmlns:th="http://www.thymeleaf.org">
	<head>
	<title>博客</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" href="css/template.css" type="text/css" />
	</head>
	<body>
	<div><a href="/postBlog">写博客</a></div>
	<div th:each="blog:${blogs}">  
	    <h2 th:utext="${blog.title}"></h2>  
	    <div th:utext="${blog.content}"></div>  
	</div> 
	</body>
	</html>

### 启动站点
如果是使用Spring Boot CLI命令行创建程序的话，会自动生成`DemoApplication.java`文件，如下：

`src/main/java/demo/DemoApplication.java`

	package demo;

	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;

	@SpringBootApplication
	public class DemoApplication {

	    public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	    }
	}

`@SpringBootApplication`是对`@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`的一个精简。`SpringApplication.run()`则是Spring Boot程序的启动入口。

使用命令行`mvn spring-boot:run`即可启动程序，浏览器中输入<http://127.0.0.1:8080/>即可访问主页，使用<http://127.0.0.1:8080/postBlog>便可发表博客。当然由于H2是内存数据库，当应用重新启动时，原先发表的博客便会被清除。
