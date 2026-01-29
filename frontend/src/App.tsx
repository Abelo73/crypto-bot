import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { Layout } from "./components/layout/Layout";
import { DashboardPage } from "./pages/DashboardPage";
import { StrategiesPage } from "./pages/StrategiesPage";
import { TerminalPage } from "./pages/TerminalPage";

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/strategies" element={<StrategiesPage />} />
          <Route path="/social" element={<div className="text-white">Social Hub Coming Soon...</div>} />
          <Route path="/terminal" element={<TerminalPage />} />
          <Route path="/settings" element={<div className="text-white">Security Settings Coming Soon...</div>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;