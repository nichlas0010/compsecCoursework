package rocks.ashleigh.lovejoy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import rocks.ashleigh.lovejoy.datastructures.EvaluationRequest;
import rocks.ashleigh.lovejoy.datastructures.LoginForm;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;
import rocks.ashleigh.lovejoy.jpa.EvaluationEntity;
import rocks.ashleigh.lovejoy.jpa.EvaluationRepository;
import rocks.ashleigh.lovejoy.jpa.UserEntity;
import rocks.ashleigh.lovejoy.jpa.UserRepository;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class LoveJoyController {
    private UserRepository userRepo;
    private EvaluationRepository evalRepo;
    private Properties prop = new Properties();
    private Random random = new Random();
    private JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    public LoveJoyController(UserRepository userRepo, EvaluationRepository evalRepo, @Value("${EMAIL_USERNAME}") String emailUser, @Value("${EMAIL_PASSWORD}") String emailPass) {
        this.userRepo = userRepo;
        this.evalRepo = evalRepo;

        mailSender.setUsername(emailUser);
        mailSender.setPassword(emailPass);

        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(465);

        Properties mailProp = mailSender.getJavaMailProperties();
        mailProp.put("mail.transport.protocol", "smtp");
        mailProp.put("mail.smtp.auth", "true");
        mailProp.put("mail.smtp.starttls.enable", "true");
        mailProp.put("mail.smtp.starttls.required", "true");
        mailProp.put("mail.debug", "true");
        mailProp.put("mail.smtp.ssl.enable", "true");
        mailProp.put("mail.smtp.user", emailUser);

    }

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
        if (form.computeValidity(userRepo)) {

            UserEntity userEntity = new UserEntity(form, String.valueOf(random.nextInt(12412953)));
            userRepo.save(userEntity);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(userEntity.getEmailAddress());
                helper.setSubject("Email confirmation");
                helper.setText(
                        "Dear "+userEntity.getName()+",please click <a href='lovejoy.ashleigh.rocks/confirmemail?address=" +
                        userEntity.getEmailAddress() +  "&token=" + userEntity.getToken() + "'>HERE</a> to confirm your email!",
                        true);

                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return "emailconfirmation";
        }

        form.setPassword("");
        form.setPasswordConfirmation("");
        model.addAttribute("user", form);
        model.addAttribute("errors", form.getErrors());
        return "registration";
    }

    @GetMapping("/confirmemail")
    public String confirmEmail(@RequestParam("address") String name, @RequestParam("token") String token) {
        UserEntity user = userRepo.findByEmailAddress(name);
        if (user == null) {
            return "redirect:/";
        }

        if (user.getToken().equals(token)) {
            user.setEmailConfirmed(true);
            userRepo.save(user);
            return "successemail";
        }
        return "failemail";


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

        UserEntity userEntity = userRepo.findByEmailAddress(form.getEmailAddress());
        if (userEntity.comparePassword(form.getPassword())) {
            session.setAttribute("login", userEntity.getEmailAddress());
            if (userEntity.isAdmin()) {
                session.setAttribute("admin", true);
            }
            return "redirect:/";
        }
        form.setPassword("");
        model.addAttribute("user", form);
        model.addAttribute("error", "Incorrect email address or password!");
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
    public String requestPage(Model model, HttpSession session) {
        if (session.getAttribute("login") == null) {
            return "redirect:/";
        }
        model.addAttribute("request", new EvaluationRequest());
        return "request";
    }

    @PostMapping("/submitrequest")
    public String submitRequest(@ModelAttribute EvaluationRequest request, HttpSession session, Model model) {
        if (session.getAttribute("login") == null) {
            return "redirect:/";
        }
        if (request.computeValidity()) {
            EvaluationEntity entity = new EvaluationEntity(userRepo.findByEmailAddress((String) session.getAttribute("login")), request);
            evalRepo.save(entity);
            return "requested";
        }
        model.addAttribute("request", request);
        model.addAttribute("errors", request.getErrors());
        return "request";
    }

    // TODO
    @GetMapping("/evaluationrequests")
    public String evaluationPage(HttpSession session, Model model) {
        if (session.getAttribute("login") == null || session.getAttribute("admin") == null) {
            return "redirect:/";
        }

        ArrayList<EvaluationEntity> evals = new ArrayList<>(evalRepo.findAll());
        HashMap<EvaluationEntity, UserEntity> map = new HashMap<>();
        for (EvaluationEntity eval : evals) {
            map.put(eval, userRepo.findByEmailAddress(eval.getEmailAddress()));
        }
        model.addAttribute("requests", map);
        return "evaluationlist";
    }

    @RequestMapping(value = "/imgs/{img_id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("img_id") String s) {
        Optional<EvaluationEntity> opt = evalRepo.findById(Integer.valueOf(s));
        if (!opt.isPresent()) {
            return new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        EvaluationEntity e = opt.get();
        return new ResponseEntity<>(e.getImage(), new HttpHeaders(), HttpStatus.OK);
    }

}
