package com.gramevapp;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class WebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private String adminPasword = "admin";
    private String userTestPassword = "userTestPassword";
    private String userTestName = "utn1";
    private String grammarName = "Grammar name";
    private String datasetName = "Grammar name";
    private String grammarDescription = "Grammar description";
    private String datasetDescription = "Grammar description";
    private String grammarText = "<func> ::= <expr>\n" +
            "<expr> ::=  <var> | <expr> <op> <expr> <op> <expr> <op> <expr>| <expr> <op> <expr> <op> <expr>\n" +
            "<op> ::= +|-|/|*\n" +
            "<var> ::= X1|X2|X3|X4|X5|X6|X7|X8|X9|X10";
    @Autowired
    private ExperimentService experimentService;
    @Autowired
    private UserService userService;
    @Autowired
    private RunService runService;
    @Autowired
    private DiagramDataService diagramDataService;
    @Autowired
    private GrammarRepository grammarRepository;
    private static User userTest;
    private static Grammar grammarTest;
    private static Dataset datasetTest;
    private static Experiment experimentRunTest;
    private static Experiment experimentSaveTest;


    @BeforeAll
    public static void setupClass() {
        GramevApplication.start();

        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 5);
        // Add connection
        driver.get("http://127.0.0.1:8182/");

    }

    @AfterAll
    public static void teardownClass() {
        GramevApplication.stop();
        if(driver!=null){
            driver.quit();;
        }
    }

    @AfterEach
    public void teardown() throws InterruptedException {

        Thread.sleep(1000);
    }

    @Test
    @Order(1)
    public void adminLogin() {
        driver.findElement(By.id("loginButton")).click();
        //login page
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys(adminPasword);
        driver.findElement(By.id("login-submit")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/admin");
    }

    @Test
    @Order(2)
    public void adminCreateUserTest() {
        driver.get("http://127.0.0.1:8182/admin");
        driver.findElement(By.id("userRegistrationPart")).click();

        //registration page
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/admin/registrationPage");
        User user = userService.findByUsername(userTestName);
        while (user != null) {
            userTestName = userTestName.substring(0, 3) +
                    (Integer.parseInt(userTestName.substring(3)) + 1);
            user = userService.findByUsername(userTestName);
        }
        driver.findElement(By.id("username")).sendKeys(userTestName);
        driver.findElement(By.id("email")).sendKeys(userTestName + "@testUser.com");
        driver.findElement(By.id("confirmEmail")).sendKeys(userTestName + "@testUser.com");
        driver.findElement(By.id("password")).sendKeys(userTestPassword);
        driver.findElement(By.id("confirmPassword")).sendKeys(userTestPassword);
        driver.findElement(By.id("institution")).sendKeys("userTestInstitution");
        driver.findElement(By.id("firstName")).sendKeys("admin");
        driver.findElement(By.id("lastName")).sendKeys("admin");
        driver.findElement(By.id("register-submit")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/admin?messageUserCreated=User+created");
        userTest = userService.findByUsername(userTestName);
        assertTrue(userTest != null);
    }

    @Test
    @Order(3)
    public void adminLogOutTest() {
        driver.findElement(By.id("logOutButton")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/login?logout");
    }

    @Test
    @Order(4)
    public void userLoginTest() {
        driver.get("http://127.0.0.1:8182/login");
        driver.findElement(By.id("username")).sendKeys(userTest.getUsername());
        driver.findElement(By.id("password")).sendKeys(userTestPassword);
        driver.findElement(By.id("login-submit")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/user");
    }

    @Test
    @Order(5)
    public void userCreateGrammarTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("grammarPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/grammar/grammarRepository");
        driver.findElement(By.id("newGrammarButton")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/grammar/grammarDetail");

        driver.findElement(By.id("grammarName")).sendKeys(grammarName);
        driver.findElement(By.id("grammarDescription")).sendKeys(grammarDescription);
        driver.findElement(By.id("fileText")).sendKeys(grammarText);
        driver.findElement(By.id("saveGrammar")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/grammar/saveGrammar");
        assertTrue(!grammarRepository.findByUserId(userTest.getId()).isEmpty());
        grammarTest = grammarRepository.findByUserId(userTest.getId()).get(0);
    }

    @Test
    @Order(6)
    public void userCreateDatasetTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("datasetPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/datasets/list");
        driver.findElement(By.id("newDatasetButton")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/dataset/datasetDetail?");
        //remove readonly
        ((JavascriptExecutor) driver).executeScript("document.getElementById('typeFile').removeAttribute('required')");
        ((JavascriptExecutor) driver).executeScript("document.getElementById('info').removeAttribute('readonly')");
        //send dataset
        driver.findElement(By.id("info")).sendKeys("#Y;X1;X2;X3;X4;X5;X6;X7;X8;X9;X10\n" +
                "0.181661491;-0.757641864;-0.797268373;-0.752628894;0.539573082;0.915534116;-0.703823191;0.405527234;-0.428163903;0.782998996;0.977554288\n" +
                "0.136220838;0.138745661;0.181301285;0.136174049;-0.407490043;-0.031498898;0.337368717;-0.521981114;0.562198107;-0.164102873;-0.094494054\n" +
                "0.06544589;0.441883455;0.488072436;0.438596642;-0.229658201;-0.353697211;0.018651407;-0.840747185;0.865107617;-0.471085092;-0.291677978\n" +
                "0.252492833;0.980872438;0.96471238;0.977583668;-0.690672238;-0.814680987;0.557669341;-0.364229966;0.341592207;-0.947693137;-0.752660874\n" +
                "1.014220216;0.609379297;-0.039777685;0.34375507;-0.194795307;-0.224786164;0.328996969;0.257379877;-0.147216115;-0.680791038;0.762075311\n" +
                "-0.050509124;-0.405535342;-0.414825315;-0.400527674;0.1784824;0.30298133;-0.072228008;0.752653141;-0.793291824;0.436048824;0.365733344\n" +
                "0.091007442;0.072448656;-0.518198536;0.838053348;-0.745606557;-0.714199066;0.854601813;0.805427637;-0.670053896;-0.129705152;0.298239195\n" +
                "0.252942895;-0.769057027;-0.785369687;-0.772345737;0.559409755;0.935370564;-0.692290658;0.385813942;-0.40832574;0.80234691;0.997390574\n" +
                "-2.301381609;0.643580178;-0.709617834;0.3816685;-0.687532949;-0.456377134;-0.174628831;0.243494493;-0.198632907;-0.873773251;-0.469561874\n" +
                "-1.987596727;-0.704375514;-0.314736739;-0.743058396;-0.082894074;0.273677442;-0.822033793;0.567714671;0.680467233;-0.716027339;-0.126330219\n" +
                "\n");

        driver.findElement(By.id("dataTypeName")).sendKeys(datasetName);
        driver.findElement(By.id("dataTypeDescription")).sendKeys(datasetDescription);
        driver.findElement(By.id("saveDataset")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/dataset/saveDataset");
        assertTrue(!experimentService.findAllExperimentDataTypeByUserId(userTest.getId()).isEmpty());
        datasetTest = experimentService.findAllExperimentDataTypeByUserId(userTest.getId()).get(0);
    }

    @Test
    @Order(7)
    public void userCreateExperimentTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("myExperimentPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/experiment/experimentRepository");
        driver.findElement(By.id("newExperimentButton")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/experiment/configExperiment?");

        driver.findElement(By.id("experimentName")).sendKeys("experimentName");
        driver.findElement(By.id("experimentDescription")).sendKeys("experimentDescription");
        driver.findElement(By.id("fileText")).sendKeys(grammarText);
        Select datasetSelect = new Select(driver.findElement(By.id("datasetId")));
        datasetSelect.selectByVisibleText(datasetName + " - " + datasetDescription);

        driver.findElement(By.id("runExperimentButton")).click();
        assertTrue(driver.getCurrentUrl().contains("expRepoSelected"));
        assertTrue(!experimentService.findByUser(userTest).isEmpty());
        experimentRunTest = experimentService.findByUser(userTest).get(0);
    }

    @Test
    @Order(8)
    public void userCloneAndSaveExperimentTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("myExperimentPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/experiment/experimentRepository");
        driver.findElement(By.name("loadExperimentButton")).click();
        assertTrue(driver.getCurrentUrl().contains("expRepoSelected"));
        driver.findElement(By.id("cloneExperimentButton")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/experiment/start");
        driver.findElement(By.id("saveExperimentButton")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/experiment/start");
        for (Experiment experiment : experimentService.findByUser(userTest)) {
            if (experiment != experimentRunTest) {
                experimentSaveTest = experiment;
            }
        }
    }

    @Test
    @Order(9)
    public void userRemoveExperimentTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("myExperimentPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/experiment/experimentRepository");
        driver.findElement(By.name("deleteButton")).click();
        checkAlert();

        driver.findElement(By.name("deleteButton")).click();
        checkAlert();
        assertTrue(driver.findElements(By.name("deleteButton")).isEmpty());
        assertTrue(experimentService.findByUser(userTest).isEmpty());
    }

    @Test
    @Order(10)
    public void userRemoveGrammarTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("grammarPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/grammar/grammarRepository");
        driver.findElement(By.name("deleteGrammarButton")).click();
        checkAlert();
        assertTrue(driver.findElements(By.name("deleteGrammarButton")).isEmpty());
        assertTrue(grammarRepository.findByUserId(userTest.getId()).isEmpty());
    }

    @Test
    @Order(11)
    public void userRemoveDatasetTest() {
        driver.get("http://127.0.0.1:8182/user");
        driver.findElement(By.id("datasetPart")).click();
        assertEquals(driver.getCurrentUrl(), "http://127.0.0.1:8182/datasets/list");
        driver.findElement(By.name("deleteDatasetButton")).click();
        checkAlert();
        assertTrue(driver.findElements(By.name("deleteDatasetButton")).isEmpty());
        assertTrue(experimentService.findAllExperimentDataTypeByUserId(userTest.getId()).isEmpty());
    }

    @Test
    @Order(12)
    public void userLogOutTest() {
        adminLogOutTest();
    }

    private void checkAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 3);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
