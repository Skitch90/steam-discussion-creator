package it.alesc.steam.pages

import it.alesc.steam.components.DiscussionTopic
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.time.Duration

class TradingForumPage(driver: WebDriver): BasePage(driver) {
    private val logger = LoggerFactory.getLogger(TradingForumPage::class.java)

    @FindBy(css = ".apphub_AppName")
    private val appName: WebElement? = null
    @FindBy(css = ".forum_topic:not(.locked)")
    private val topics: List<WebElement>? = null
    @FindBy(css = ".btn_medium.responsive_OnClickDismissMenu")
    private val startDiscussionButton: WebElement? = null
    @FindBy(css = "input[name='topic']")
    private val titleInputText: WebElement? = null
    @FindBy(css = "textarea[name='text']")
    private val textArea: WebElement? = null
    @FindBy(css = "button[type='submit']")
    private val createDiscussionButton: WebElement? = null

    fun navigate(appID: String) {
        navigateTo("https://steamcommunity.com/app/$appID/tradingforum/")
    }

    fun getAppName(): String {
        return appName?.text ?: "N/A"
    }
    fun existsDiscussion(predicate: (DiscussionTopic) -> Boolean): Boolean {
        return getDiscussions().any(predicate)
    }

    private fun getDiscussions(): List<DiscussionTopic> {
        return topics?.map { DiscussionTopic(it) } ?: listOf()
    }

    fun startDiscussion() {
        clickElement(startDiscussionButton!!)
    }

    fun typeTitle(title: String) {
        titleInputText!!.sendKeys(title)
    }

    fun typeText(text: String) {
        textArea!!.sendKeys(text)
    }

    fun createDiscussion(simulation: Boolean): Boolean {
        if (simulation) {
            logger.info("Simulation enabled, avoid click on \"Create discussion\"")
            return false;
        }

        try {
            clickElement(createDiscussionButton!!)
            WebDriverWait(driver, Duration.ofSeconds(2))
                .until { it.findElement(By.cssSelector(".forum_topic_needs_content_check_notice.forum_newtopic_box")) }
            logger.info("Discussion created for game \"{}\"", getAppName())
            return true;
        } catch (e: Exception) {
            val errorElement: WebElement? = driver.findElement(By.className("forum_newtopic_error"))
            errorElement?.let {
                logger.error("Error creating discussion for game \"{}\": \"{}\"", getAppName(), it.text)
            } ?: throw e
            return false;
        }
    }
}