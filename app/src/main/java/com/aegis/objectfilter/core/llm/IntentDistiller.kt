package com.aegis.objectfilter.core.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class IntentDistiller(
  private val modelPathOnDevice: String,
) : AutoCloseable {

  private var llm: LlmInference? = null

  fun init(context: Context) {
    if (llm != null) return

    val options = LlmInference.LlmInferenceOptions.builder()
      .setModelPath(modelPathOnDevice)
      .setMaxTopK(40)
      .build()

    llm = LlmInference.createFromOptions(context, options)
  }

  fun distill(utterance: String): String {
    val prompt = buildPrompt(utterance)
    val result = llm?.generateResponse(prompt)
    return result ?: ""
  }

  private fun buildPrompt(utterance: String): String {
    return """
You are an in-vehicle safety intent distiller for an elderly driver.
Summarize the driver utterance into one bracketed tag.
Rules:
- Output MUST be exactly one tag like: [Warning: ...] or [Info: ...]
- Keep it short.

Utterance: $utterance
Output:
""".trimIndent()
  }

  override fun close() {
    llm?.close()
    llm = null
  }
}
