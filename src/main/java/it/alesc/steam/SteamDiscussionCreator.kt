package it.alesc.steam

import com.sksamuel.hoplite.ConfigLoader
import it.alesc.steam.config.Config
import it.alesc.steam.config.Discussion
import it.alesc.steam.db.DBUtils.configureDatabase
import it.alesc.steam.db.DBUtils.postInsertedRecently
import it.alesc.steam.db.DBUtils.trackInsertedPost
import it.alesc.steam.pages.LoginPage
import it.alesc.steam.pages.TradingForumPage
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.slf4j.LoggerFactory
import java.time.Duration

object SteamDiscussionCreator {
    private val logger = LoggerFactory.getLogger(SteamDiscussionCreator::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val executionConfiguration = ConfigLoader().loadConfigOrThrow<Config>("/config.yaml")
        val driver = configureDriver()
        configureDatabase()

        performLogin(driver, executionConfiguration)
        createDiscussions(driver, executionConfiguration)

        driver.quit()
    }

    private fun createDiscussions(driver: WebDriver, executionConfiguration: Config) {
        val tradingForumPage = TradingForumPage(driver)
        executionConfiguration.apps.forEach {
            createDiscussionForApp(tradingForumPage, it, executionConfiguration.discussion)
        }
    }

    private fun performLogin(driver: WebDriver, executionConfiguration: Config) {
        val loginPage = LoginPage(driver)
        loginPage.navigate()
        val (username, password) = executionConfiguration.credentials
        loginPage.insertCredentials(username, password)
        loginPage.login()
    }

    private fun createDiscussionForApp(tradingForumPage: TradingForumPage, appID: String, discussion: Discussion) {
        tradingForumPage.navigate(appID)
        if (tradingForumPage.existsDiscussion { it.getAuthor() == discussion.author }) {
            logger.info("Discussion already exists for game \"{}\"", tradingForumPage.getAppName())
            return
        }
        if (postInsertedRecently(appID)) {
            logger.info("Post inserted recently for game \"{}\"", tradingForumPage.getAppName())
            return
        }
        tradingForumPage.startDiscussion()
        tradingForumPage.typeTitle(discussion.title)
        tradingForumPage.typeText(discussion.text)
        tradingForumPage.createDiscussion(discussion.simulation)
        trackInsertedPost(appID, discussion.simulation)
    }

    private fun configureDriver(): WebDriver {
        val driver: WebDriver = FirefoxDriver()
        driver.manage()
            .timeouts()
            .implicitlyWait(Duration.ofSeconds(5))
        return driver
    }
}