package it.alesc.steam.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.PageFactory

abstract class BasePage(protected val driver: WebDriver) {
    init {
        PageFactory.initElements(driver, this)
    }

    fun navigateTo(url: String?) {
        driver.navigate().to(url)
    }

    fun clickElement(element: WebElement) {
        element.click()
    }
}