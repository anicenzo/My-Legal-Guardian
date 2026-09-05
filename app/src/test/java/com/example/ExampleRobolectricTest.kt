package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Legal AI — Contract Scanner", appName)
  }

  @Test
  fun `legal audit engine dynamically identifies present safeguards`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = com.example.engine.LegalAuditEngine(context)

    val protectedLease = """
      RESIDENTIAL LEASE AGREEMENT
      1. Tenant is entitled to peaceful possession and quiet enjoyment of the premises.
      2. Landlord agrees to maintain the premises in good repair and warranty of habitability.
      3. The security deposit shall be returned within 21 days after vacating.
      4. In the event of default, tenant shall receive a 30-day notice to cure any default.
      5. Either party may terminate this agreement upon 30 days mutual written notice.
    """.trimIndent()

    val result = engine.analyzeContract(protectedLease)
    // All 5 safeguards were explicitly present in the text, so missing list must be empty!
    assertEquals(0, result.missingMandatoryClauses.size)

    val predatoryLease = """
      STANDARD COMMERCIAL LEASE
      1. Tenant assumes all liability and waives all rights.
      2. Landlord may terminate at sole discretion.
      3. Late fee of $500 applied immediately with no grace period.
    """.trimIndent()

    val predatoryResult = engine.analyzeContract(predatoryLease)
    // No safeguards were included, so all 5 mandatory safeguards must be flagged as missing!
    assertEquals(5, predatoryResult.missingMandatoryClauses.size)
  }
}
