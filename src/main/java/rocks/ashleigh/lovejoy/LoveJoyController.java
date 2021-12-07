package rocks.ashleigh.lovejoy;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import rocks.ashleigh.lovejoy.datastructures.LoginForm;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;
import javax.servlet.http.HttpSession;

@Controller
public class LoveJoyController {

    // DONE
    @GetMapping("/")
    public String mainPage(Model model, HttpSession session) {
        model.addAttribute("login", session.getAttribute("login"));
        model.addAttribute("admin", session.getAttribute("admin"));
        return "main";
    }

    @GetMapping("/*")
    public RedirectView badURL() {
        return new RedirectView("/");
    }

    /** -- REGISTRATION -- **/
    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "registration";
    }

    @RequestMapping(value = "/registeruser", method = RequestMethod.POST)
    public String registerUser(@ModelAttribute RegistrationForm form, Model model) {
        if (form.computeValidity()) {
            // TODO: Actually register the user
            return "emailconfirmation";
        }

        form.setPassword("");
        form.setPasswordConfirmation("");
        model.addAttribute("user", form);
        model.addAttribute("errors", form.getErrors());
        return "registration";
    }


    /** -- LOGIN -- **/
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute("login") != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new LoginForm());
        return "login";
    }

    @RequestMapping(value = "/loginuser", method = RequestMethod.POST)
    public String loginUser(@ModelAttribute LoginForm form, Model model, HttpSession session) {
        if (form.computeValidity()) {
            session.setAttribute("login", true);
            if (form.isAdmin()) {
                session.setAttribute("admin", true);
            }
            return "redirect:/";
        }

        form.setPassword("");
        model.addAttribute("user", form);
        model.addAttribute("error", "Incorrect username or password!");
        return "login";
    }

    @GetMapping("/logout")
    public RedirectView logoutPage(HttpSession session) {
        session.removeAttribute("login");
        session.removeAttribute("admin");
        return new RedirectView("/");
    }

    // TODO
    @GetMapping("/passwordrecovery")
    public String recoveryPage() {
        return "recovery";
    }

    // TODO
    @GetMapping("/requestevaluation")
    public String requestPage(HttpSession session) {
        if (session.getAttribute("login") == null) {
            return "redirect:/";
        }
        return "request";
    }

    // TODO
    @GetMapping("/evaluationrequests")
    public String evaluationPage(HttpSession session) {
        if (session.getAttribute("login") == null || session.getAttribute("admin") == null) {
            return "redirect:/";
        }
        return "evaluation";
    }

    // TODO: Get rid of
    @GetMapping("/hello")
    public String helloWorld(@RequestParam(name="name", defaultValue = "World", required = false) String name, Model model) {
        model.addAttribute("name", name);
        return "hello";
    }

}
