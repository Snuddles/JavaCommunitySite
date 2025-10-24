package com.jcs.javacommunitysite.pages;
import com.jcs.javacommunitysite.forms.NewPostForm;
import com.jcs.javacommunitysite.pages.homepage.HomepageController; // fix the package
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    private final HomepageController hpc; // or a Service, ideally

    public PageController(HomepageController homepageController) {
        this.hpc = homepageController;
    }

//    @GetMapping("/browse")
//    public String home(Model model) {
//
//        model.addAttribute("groupsWithCategories", hpc.getGroupsCategories());
//        return "pages/browse";
//    }

    @GetMapping("/groups/{groupName}")
    public String groupPage(@PathVariable String groupName, Model model) {
        model.addAttribute("groupName", groupName);
        return "pages/groupCategories";
    }

    // NEW ENDPOINTS

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/ask")
    public String ask(
            Model model
    ) {
        model.addAttribute("postForm", new NewPostForm());
        return "pages/ask";
    }

    @GetMapping("/answer")
    public String answer() {
        return "pages/answer";
    }

    @GetMapping("/search")
    public String search() {
        return "pages/search";
    }

    @GetMapping("/chat")
    public String chat() {
        return "pages/chat";
    }
}
