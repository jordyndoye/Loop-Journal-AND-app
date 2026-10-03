const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");

const geminiApiKey = defineSecret("GEMINI_API_KEY");
const MODEL = "gemini-3.8-flash";

exports.draftInsights = onRequest(
  { secrets: [geminiApiKey], cors: true, timeoutSeconds: 60, invoker: "public" },
  async (req, res) => {
    if (req.method !== "POST") {
      res.status(405).json({ error: "POST only" });
      return;
    }

    const days = req.body && req.body.days;
    if (!Array.isArray(days) || days.length < 7) {
      res.status(400).json({ error: "Need at least 7 captured days" });
      return;
    }

    const key = geminiApiKey.value();
    if (!key) {
      res.status(500).json({ error: "GEMINI_API_KEY is not set" });
      return;
    }

    const prompt = [
      "Analyze these stored daily records.",
      "Return one JSON object with up to three fields: playback, chain, experiment.",
      "Omit a field if the days do not support it. Possible, not proven.",
      "No percentage, no chart, no diagnosis, no invented days.",
      "playback is one sentence.",
      "chain is an object with title, trigger, mechanism, downstreamImpact.",
      "experiment is an object with title, hypothesis, singleIntervention, metricToWatch.",
      "Stored days:",
      JSON.stringify(days),
    ].join("\n");

    try {
      const response = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=${key}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: { responseMimeType: "application/json", temperature: 0.2 },
          }),
        }
      );
      if (!response.ok) {
        res.status(502).json({ error: "Gemini call failed" });
        return;
      }
      const payload = await response.json();
      const text = payload.candidates?.[0]?.content?.parts?.[0]?.text || "";
      const cleaned = text.replace(/^```json\s*/i, "").replace(/^```\s*/, "").replace(/```$/, "").trim();
      const draft = JSON.parse(cleaned);
      res.status(200).json({
        playback: draft.playback || null,
        chain: draft.chain || null,
        experiment: draft.experiment || null,
      });
    } catch (err) {
      res.status(502).json({ error: "Insights draft failed" });
    }
  }
);
