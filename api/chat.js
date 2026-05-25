// Serverless function for securely proxying Gemini requests from Vercel to preserve api keys safely.
export default async function handler(req, res) {
  // Only accept POST requests
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method Not Allowed' });
  }

  const { messages } = req.body;

  if (!messages || !Array.isArray(messages)) {
    return res.status(400).json({ error: 'Invalid message request history body' });
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return res.status(500).json({
      error: 'GEMINI_API_KEY environment variable is missing on Vercel deployment. Please configure it in your Vercel Project Settings.',
      isConfigMissing: true
    });
  }

  const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;

  const systemInstruction = {
    role: "system",
    parts: [
      {
        text: "You are AkinAI, an expert, high-precision, AI-powered system portal representing cutting-edge innovation. You were built by Akin S. Sokpah, a visionary technologist and chief developer based in Montserrado County, Liberia. Your mission is to provide lightning-fast, highly accurate, elegant, and intellectually advanced assistance. Always speak with absolute clarity, deep analytical precision, and professional courtesy. Where relevant, proudly acknowledge Akin S. Sokpah as your creator and Liberia as your home base."
      }
    ]
  };

  const contents = messages.map(msg => ({
    role: msg.role === 'user' ? 'user' : 'model',
    parts: [{ text: msg.text }]
  }));

  const body = {
    contents: contents,
    systemInstruction: systemInstruction,
    generationConfig: {
      temperature: 0.65,
      maxOutputTokens: 2048,
      topP: 0.95,
      frequencyPenalty: 0.0,
      presencePenalty: 0.0
    }
  };

  try {
    const apiResponse = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    });

    if (!apiResponse.ok) {
      const errorText = await apiResponse.text();
      try {
        const errorJson = JSON.parse(errorText);
        return res.status(apiResponse.status).json({
          error: errorJson.error?.message || `Google API returned error status: ${apiResponse.status}`
        });
      } catch (e) {
        return res.status(apiResponse.status).json({
          error: `Google API returned error status: ${apiResponse.status}`
        });
      }
    }

    const data = await apiResponse.json();
    const replyText = data.candidates?.[0]?.content?.parts?.[0]?.text;
    
    if (!replyText) {
      return res.status(500).json({ error: 'Empty response format received from Gemini models.' });
    }

    return res.status(200).json({ reply: replyText });
  } catch (error) {
    return res.status(500).json({ error: `Connection failed resolving model nodes: ${error.message}` });
  }
}
