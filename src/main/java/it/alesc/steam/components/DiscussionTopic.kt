package it.alesc.steam.components

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class DiscussionTopic(root: WebElement): BaseComponent(root) {
    fun getAuthor(): String {
        return root.findElement(By.className("forum_topic_op")).text
    }
}