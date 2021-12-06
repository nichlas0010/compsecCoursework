package rocks.ashleigh.lovejoy;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;

@Controller
public class LoveJoyController {

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/*")
    public RedirectView badURL() {
        return new RedirectView("/");
    }

    /** -- REGISTRATION -- **/
    // TODO
    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "registration";
    }

    // TODO
    @RequestMapping(value = "/registeruser", method = RequestMethod.POST)
    public String registerUser(@ModelAttribute RegistrationForm form, Model model) {
        if (form.computeValidity()) {
            return "emailConfirmation";
        }

        form.setPassword("");
        form.setPasswordConfirmation("");
        model.addAttribute("user", form);
        return "registration";
    }


    // TODO
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // TODO
    @GetMapping("/logout")
    public RedirectView logoutPage() {
        return new RedirectView("/");
    }

    // TODO
    @GetMapping("/passwordrecovery")
    public String recoveryPage() {
        return "recovery";
    }

    // TODO
    @GetMapping("/requestevaluation")
    public String requestPage() {
        return "request";
    }

    // TODO
    @GetMapping("/evaluationrequests")
    public String evaluationPage() {
        return "evaluation";
    }

    // TODO: Get rid of
    @GetMapping("/hello")
    public String helloWorld(@RequestParam(name="name", defaultValue = "World", required = false) String name, Model model) {
        model.addAttribute("name", name);
        return "hello";
    }

}
