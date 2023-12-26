package it.alesc.steam.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class HomePage(driver: WebDriver): BasePage(driver) {

    init {
        WebDriverWait(driver, Duration.ofSeconds(20))
            .until { it.findElement(By.cssSelector("#account_pulldown")) }
    }

}