package com.example

import com.example.ui.AppLanguage
import com.example.ui.getStrings
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testLocalizationStrings() {
    val en = getStrings(AppLanguage.ENGLISH)
    val hi = getStrings(AppLanguage.HINDI)
    val hinglish = getStrings(AppLanguage.HINGLISH)

    assertEquals("Calendar", en.calendar)
    assertEquals("कैलेंडर", hi.calendar)
    assertEquals("Calendar", hinglish.calendar)

    assertEquals("Today's Progress", en.todaysProgress)
    assertEquals("आज की प्रगति", hi.todaysProgress)
    assertEquals("Aaj ki Progress", hinglish.todaysProgress)

    assertEquals("Student Study Mode", en.studentMode)
    assertEquals("विद्यार्थी अध्ययन मोड", hi.studentMode)
    assertEquals("Student Study Mode", hinglish.studentMode)
  }

  @Test
  fun testProgressCalculation() {
    val total = 5
    val completed = 4
    val percentage = (completed * 100) / total
    assertEquals(80, percentage)
  }
}
