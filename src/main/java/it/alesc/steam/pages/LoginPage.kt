package it.alesc.steam.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class LoginPage(driver: WebDriver): BasePage(driver) {

    @FindBy(css = "form[class^='newlogindialog'] > div > input[type='text']")
    private val usernameTextField: WebElement? = null

    @FindBy(css = "input[type='password']")
    private val passwordTextField: WebElement? = null

    @FindBy(css = "button[type='submit']")
    private val signInButton: WebElement? = null

    fun navigate() {
        navigateTo("https://store.steampowered.com/login/")
    }

    fun insertCredentials(username: String, password: String) {
        usernameTextField!!.sendKeys(username)
        passwordTextField!!.sendKeys(password)
    }

    fun login() {
        clickElement(signInButton!!)
        HomePage(driver)
    }

}