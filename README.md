# 🎯 Draft Predictor

**A League of Legends analytics platform** that collects ranked match data from the Riot API, processes champion draft information, and builds a **draft prediction system** based on real statistical patterns.

> 🔗 [http://194.33.35.224:8083/lol/main](http://194.33.35.224:8083/lol/main)

---

## 📖 About the Project

Draft Predictor is a data-driven analytics platform for **League of Legends**. It automatically gathers ranked SoloQ match data via the Riot API, extracts both **basic match parameters** and **detailed timeline events**, and uses this information to build a statistical model that predicts optimal champion drafts.

The core idea is simple: instead of relying on intuition, the system analyzes thousands of matches to discover real patterns — champion synergy, pick/ban rates, win rates, counter-picks, and timeline-based power spikes — and turns them into actionable draft recommendations.

---

## ✨ Features

### 📊 Data Collection Pipeline
- Automated daily collection of **SoloQ ranked matches** via the Riot API.
- Extraction of **basic match parameters** (champions, roles, outcomes, bans, etc.).
- Extraction of **extended timeline parameters** (gold/xp graphs, power spikes, early/mid/late game transitions).
- Fully scheduled pipeline using Spring `@Scheduled` annotations — runs hands-free every day.

### 🧠 Draft Prediction System
- Statistical analysis of champion performance across patches and roles.
- Counter-pick recommendations based on win-rate differentials.
- Best duo / best trio detection (top side & bot side synergies).
- Pick/ban rate analytics and draft presence metrics.
- Flexibility scoring per champion per role.
- Interactive draft board with real-time suggestions.

### 🎨 Interactive Frontend
- Dynamic draft board built with **HTML5 Canvas**.
- Real-time data fetching via **Fetch API**.
- Champion icons, role filters, patch selectors, and region filters.
- Responsive UI with vanilla **JavaScript + CSS**.

---

## 🏗️ How It Works

1. **Scheduled jobs** trigger daily data collection from the Riot API.
2. **WebClient** performs non-blocking HTTP requests to fetch match and timeline data.
3. Parsed data is stored in **PostgreSQL** using Spring-managed **transactions**.
4. **Flyway** handles all database schema migrations.
5. Analytical queries aggregate statistics (win rates, synergy, counter-picks, etc.).
6. The **frontend** visualizes the results and powers the draft prediction UI.

---

## 🛠️ Tech Stack

| Layer          | Technology                                |
|----------------|-------------------------------------------|
| **Backend**    | Spring Boot, Spring Web, Spring Data JPA  |
| **HTTP Client**| Spring WebClient (reactive, non-blocking) |
| **Database**   | PostgreSQL                                |
| **Migrations** | Flyway                                    |
| **Scheduling** | Spring `@Scheduled`                       |
| **Logging**    | Logback                                   |
| **Testing**    | JUnit, `@TestComponent`                   |
| **Frontend**   | HTML5, CSS3, JavaScript, Canvas, Fetch API|
| **External API**| Riot Games API                           |
| **Dev Tools**  | Spring DevTools                           |

---

## 🔮 Roadmap

### ✅ Current State
- SoloQ ranked match data collection.
- Timeline-based analytics.
- Draft prediction based on SoloQ statistics.

### 🚧 Planned
- **Pro match data collection** — extend the pipeline to gather data from professional League of Legends matches (LCK, LEC, LPL, LCS, etc.).
- **Hybrid draft prediction** — combine SoloQ and pro-scene statistics to produce more accurate and meta-aware draft recommendations.
- **ML-based optimization** — introduce machine learning models to refine predictions beyond pure statistical aggregation.
- **OpenCV integration** — automatic draft scanning from streams/VODs.

---

## 📦 Local Setup

```bash
# 1. Clone the repository
git clone https://github.com/kv3rk/draft-predict.git
cd draft-predict

# 2. Configure application properties
#    Set your Riot API key and PostgreSQL credentials in:
#    src/main/resources/application.properties

# 3. Run the application
./mvnw spring-boot:run
```
⚠️ A valid Riot Games API key is required to fetch match data.

---

## 📄 License
This project is built for educational and analytical purposes.
League of Legends and Riot Games are trademarks of Riot Games, Inc.
<div align="center">
<i>Built with ❤️ and a lot of ranked games</i>
</div>