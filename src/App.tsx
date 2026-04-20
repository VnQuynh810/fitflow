import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Activity, 
  Calendar, 
  Plus, 
  User, 
  Droplets, 
  Flame, 
  Footprints, 
  ChevronRight,
  PlusCircle,
  Trophy,
  Bell,
  CheckCircle2,
  Settings
} from 'lucide-react';
import { UserStats, WorkoutPlan, DailyActivity, Streak } from './types';
import { DEFAULT_EXERCISES, WATER_GOAL, STEP_GOAL } from './constants';
import { generate30DayPlan } from './services/geminiService';

// --- Sub-components (Simplified for now, will expand) ---

export default function App() {
  const [view, setView] = useState<'onboarding' | 'dashboard' | 'planner' | 'library' | 'profile'>('dashboard');
  const [stats, setStats] = useState<UserStats | null>(null);
  const [plan, setPlan] = useState<WorkoutPlan | null>(null);
  const [activity, setActivity] = useState<DailyActivity>({
    steps: 0,
    waterIntake: 0,
    lastUpdated: new Date().toISOString()
  });
  const [streak, setStreak] = useState<Streak>({ current: 0, lastWorkoutDate: null });
  const [loading, setLoading] = useState(false);

  // Persistence
  useEffect(() => {
    const savedStats = localStorage.getItem('fitflow_stats');
    const savedPlan = localStorage.getItem('fitflow_plan');
    const savedActivity = localStorage.getItem('fitflow_activity');
    const savedStreak = localStorage.getItem('fitflow_streak');

    if (savedStats) setStats(JSON.parse(savedStats));
    if (savedPlan) setPlan(JSON.parse(savedPlan));
    if (savedActivity) setActivity(JSON.parse(savedActivity));
    if (savedStreak) setStreak(JSON.parse(savedStreak));

    if (!savedStats) setView('onboarding');
  }, []);

  useEffect(() => {
    if (stats) localStorage.setItem('fitflow_stats', JSON.stringify(stats));
    if (plan) localStorage.setItem('fitflow_plan', JSON.stringify(plan));
    if (activity) localStorage.setItem('fitflow_activity', JSON.stringify(activity));
    if (streak) localStorage.setItem('fitflow_streak', JSON.stringify(streak));
  }, [stats, plan, activity, streak]);

  const handleCompleteOnboarding = async (newStats: UserStats) => {
    setLoading(true);
    setStats(newStats);
    const newPlan = await generate30DayPlan(newStats);
    setPlan(newPlan);
    setLoading(false);
    setView('dashboard');
  };

  const updateStreak = () => {
    const today = new Date().toISOString().split('T')[0];
    if (streak.lastWorkoutDate === today) return;

    const lastDate = streak.lastWorkoutDate ? new Date(streak.lastWorkoutDate) : null;
    const diffDays = lastDate ? (new Date(today).getTime() - lastDate.getTime()) / (1000 * 3600 * 24) : 0;

    if (diffDays === 1 || !lastDate) {
      setStreak({ current: streak.current + 1, lastWorkoutDate: today });
    } else if (diffDays > 1) {
      setStreak({ current: 1, lastWorkoutDate: today });
    }
  };

  return (
    <div className="min-h-screen bg-black flex justify-center items-start sm:items-center">
      {/* Mobile Shell for Desktop */}
      <div className="w-full max-w-md h-full sm:h-[85vh] sm:rounded-[3rem] sm:border-[8px] sm:border-white/10 bg-bg-dark overflow-y-auto overflow-x-hidden relative shadow-2xl selection:bg-accent selection:text-black hide-scrollbar">
        <AnimatePresence mode="wait">
          {view === 'onboarding' && (
            <Onboarding 
              onComplete={handleCompleteOnboarding} 
              isLoading={loading} 
            />
          )}

          {view === 'dashboard' && stats && plan && (
            <Dashboard 
              stats={stats} 
              plan={plan} 
              activity={activity} 
              setActivity={setActivity}
              streak={streak}
            />
          )}

          {view === 'planner' && plan && (
            <Planner plan={plan} />
          )}

          {view === 'library' && (
            <Library />
          )}
        </AnimatePresence>

        {/* Bottom Navigation */}
        {view !== 'onboarding' && (
          <nav className="sticky bottom-0 left-0 right-0 bg-card-dark/80 backdrop-blur-xl border-t border-white/10 px-6 py-4 flex justify-between items-center z-50">
            <NavButton icon={Activity} active={view === 'dashboard'} onClick={() => setView('dashboard')} label="Home" />
            <NavButton icon={Calendar} active={view === 'planner'} onClick={() => setView('planner')} label="Plan" />
            <div className="bg-accent p-3 rounded-full -mt-12 shadow-lg shadow-accent/20 cursor-pointer hover:scale-110 transition-transform flex items-center justify-center">
              <Plus className="text-black w-6 h-6" />
            </div>
            <NavButton icon={PlusCircle} active={view === 'library'} onClick={() => setView('library')} label="Library" />
            <NavButton icon={User} active={view === 'profile'} onClick={() => setView('profile')} label="Me" />
          </nav>
        )}
      </div>
      
      {/* Visual background for desktop */}
      <div className="fixed inset-0 -z-10 bg-[radial-gradient(circle_at_50%_50%,#111,black)]" />
    </div>
  );
}

// --- Views ---

function Onboarding({ onComplete, isLoading }: { onComplete: (stats: UserStats) => void, isLoading: boolean }) {
  const [formData, setFormData] = useState({
    height: 170,
    weight: 65,
    gender: 'male' as const,
    goal: 'Weight Loss' as const
  });

  const bmi = formData.weight / ((formData.height / 100) ** 2);

  return (
    <motion.div 
      initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="max-w-md mx-auto p-8 pt-20"
    >
      <header className="mb-12 text-center">
        <h1 className="text-4xl font-black tracking-tighter mb-2 uppercase italic">Fit<span className="text-accent italic">Flow</span></h1>
        <p className="text-[10px] uppercase font-bold tracking-[0.2em] text-white/40">Craft your monthly journey</p>
      </header>

      <div className="space-y-8">
        <section>
          <label className="block text-[11px] font-bold text-white/40 uppercase tracking-widest mb-4">Height (cm)</label>
          <input 
            type="range" min="120" max="220" 
            value={formData.height} 
            onChange={(e) => setFormData({...formData, height: parseInt(e.target.value)})}
            className="w-full h-1 bg-white/10 rounded-full appearance-none cursor-pointer accent-accent"
          />
          <div className="text-center font-black text-3xl mt-4 italic">{formData.height}</div>
        </section>

        <section>
          <label className="block text-[11px] font-bold text-white/40 uppercase tracking-widest mb-4">Weight (kg)</label>
          <input 
            type="number" 
            value={formData.weight} 
            onChange={(e) => setFormData({...formData, weight: parseInt(e.target.value)})}
            className="w-full p-5 bg-card-dark border border-white/10 rounded-2xl text-center text-4xl font-black italic focus:ring-1 focus:ring-accent outline-none"
          />
        </section>

        <div className="bg-card-dark p-8 rounded-[2rem] text-center border border-white/10 shadow-stat-glow">
          <div className="text-[11px] font-bold text-white/40 uppercase tracking-widest mb-2">Calculated BMI</div>
          <div className="text-5xl font-black italic mb-2 tracking-tighter">{bmi.toFixed(1)}</div>
          <div className="inline-block px-3 py-1 bg-accent/10 border border-accent/20 rounded-full text-accent text-[10px] font-bold uppercase tracking-widest">
            {bmi < 18.5 ? 'Underweight' : bmi < 25 ? 'Normal weight' : bmi < 30 ? 'Overweight' : 'Obese'}
          </div>
        </div>

        <section>
          <label className="block text-[11px] font-bold text-white/40 uppercase tracking-widest mb-4">Main Goal</label>
          <div className="grid grid-cols-2 gap-3">
            {['Weight Loss', 'Muscle Gain', 'Endurance', 'Maintenance'].map((goal) => (
              <button
                key={goal}
                onClick={() => setFormData({...formData, goal: goal as any})}
                className={`p-4 rounded-xl text-[11px] font-bold uppercase tracking-wider transition-all ${
                  formData.goal === goal 
                    ? 'bg-accent text-black shadow-lg shadow-accent/10' 
                    : 'bg-white/5 border border-white/5 text-white/60 hover:bg-white/10'
                }`}
              >
                {goal}
              </button>
            ))}
          </div>
        </section>

        <button
          disabled={isLoading}
          onClick={() => onComplete({...formData, bmi})}
          className="w-full bg-accent text-black p-5 rounded-2xl font-black text-xs uppercase tracking-[0.2em] mt-8 flex items-center justify-center gap-2 hover:scale-[0.98] transition-all disabled:opacity-50"
        >
          {isLoading ? (
            <div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin" />
          ) : (
            <>Activate Journey <ChevronRight className="w-4 h-4" /></>
          )}
        </button>
      </div>
    </motion.div>
  );
}

function Dashboard({ stats, plan, activity, setActivity, streak }: { 
  stats: UserStats, 
  plan: WorkoutPlan, 
  activity: DailyActivity,
  setActivity: (activity: DailyActivity) => void,
  streak: Streak
}) {
  const today = new Date().toISOString().split('T')[0];
  const todayWorkout = plan.days[today];

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }}
      className="p-6 space-y-8 max-w-lg mx-auto"
    >
      <header className="flex justify-between items-center pt-4 border-b border-white/10 pb-6">
        <div>
          <h2 className="text-[10px] font-black text-white/30 uppercase tracking-[0.3em] mb-1">Status Report</h2>
          <h1 className="text-3xl font-black italic tracking-tighter uppercase">Rise & <span className="text-accent italic">Grind</span></h1>
        </div>
        <div className="relative p-2 bg-white/5 rounded-full border border-white/5">
          <Bell className="w-5 h-5 text-white/40" />
          <span className="absolute top-2 right-2 w-2 h-2 bg-accent border-2 border-bg-dark rounded-full"></span>
        </div>
      </header>

      {/* Streak & Stats Summary */}
      <section className="grid grid-cols-2 gap-4">
        <div className="bg-card-dark p-6 rounded-[2rem] border border-white/5 shadow-stat-glow flex flex-col justify-between h-40">
          <Trophy className="w-6 h-6 text-accent opacity-50" />
          <div>
            <div className="text-5xl font-black italic tracking-tighter text-accent">{streak.current}</div>
            <div className="text-[10px] font-bold uppercase tracking-widest text-white/40">Chuỗi Streak</div>
          </div>
        </div>
        <div className="bg-card-dark p-6 rounded-[2rem] border border-white/5 flex flex-col justify-between h-40">
          <Activity className="w-6 h-6 text-white/20" />
          <div>
            <div className="text-xl font-bold uppercase tracking-tight italic">{stats.goal}</div>
            <div className="text-[10px] font-bold uppercase tracking-widest text-white/40">Giai đoạn 1</div>
          </div>
        </div>
      </section>

      {/* Daily Tracking */}
      <section className="space-y-4">
        <h3 className="text-[11px] font-bold uppercase tracking-[0.3em] text-white/30 ml-1">Health Metrics</h3>
        <div className="space-y-3">
          <MetricCard 
            icon={Footprints} 
            color="text-accent" 
            bgColor="bg-accent/5"
            label="Steps" 
            value={activity.steps} 
            goal={STEP_GOAL} 
            unit="steps"
            onIncrement={() => setActivity({...activity, steps: activity.steps + 500})}
          />
          <MetricCard 
            icon={Droplets} 
            color="text-accent" 
            bgColor="bg-accent/5"
            label="Water" 
            value={activity.waterIntake} 
            goal={WATER_GOAL} 
            unit="ml"
            onIncrement={() => setActivity({...activity, waterIntake: activity.waterIntake + 250})}
          />
        </div>
      </section>

      {/* Today's Workout */}
      <section className="space-y-4">
        <div className="flex justify-between items-center ml-1">
          <h3 className="text-[11px] font-bold uppercase tracking-[0.3em] text-white/30">Session</h3>
          <span className="text-[10px] text-accent font-bold uppercase tracking-widest cursor-pointer hover:underline underline-offset-4">Lịch tập tháng</span>
        </div>
        {todayWorkout && todayWorkout.exercises.length > 0 ? (
          <div className="space-y-3">
             {todayWorkout.exercises.map((ex) => (
                <div key={ex.id} className="bg-card-dark p-5 rounded-[1.5rem] border border-white/5 flex items-center justify-between shadow-sm hover:border-white/20 transition-all">
                  <div className="flex items-center gap-5">
                    <div className="w-10 h-10 bg-white/5 border border-white/5 rounded-xl flex items-center justify-center text-white/20 font-black italic">
                      {ex.name.charAt(0)}
                    </div>
                    <div>
                      <h4 className="font-bold uppercase text-sm tracking-tight">{ex.name}</h4>
                      <p className="text-[10px] font-bold uppercase tracking-widest text-white/30 mt-1">
                        {ex.reps ? `${ex.reps} reps` : `${ex.duration}s`} • {ex.sets} sets
                      </p>
                    </div>
                  </div>
                  <button className="w-8 h-8 rounded-full border border-white/10 flex items-center justify-center hover:bg-accent/10 hover:border-accent group transition-all">
                    <CheckCircle2 className="w-4 h-4 text-white/10 group-hover:text-accent" />
                  </button>
                </div>
             ))}
          </div>
        ) : (
          <div className="bg-card-dark p-12 rounded-[2rem] text-center border border-white/5 border-dashed">
            <p className="text-[11px] text-white/30 font-bold uppercase tracking-widest italic opacity-50">Ngày nghỉ phục hồi</p>
          </div>
        )}
      </section>
    </motion.div>
  );
}

function MetricCard({ icon: Icon, color, bgColor, label, value, goal, unit, onIncrement }: any) {
  const progress = Math.min((value / goal) * 100, 100);
  return (
    <div className="bg-card-dark p-5 rounded-[1.5rem] border border-white/5 flex items-center gap-6 shadow-sm overflow-hidden">
      <div className={`${bgColor} p-3 rounded-2xl border border-white/5`}>
        <Icon className={`w-5 h-5 ${color}`} />
      </div>
      <div className="flex-1">
        <div className="flex justify-between items-end mb-2">
          <span className="text-[10px] font-black text-white/20 uppercase tracking-[0.2em]">{label}</span>
          <span className="text-xs font-bold font-mono tracking-tighter">
            <span className="text-white">{value}</span>
            <span className="text-white/20 ml-1 italic">/ {goal}</span>
          </span>
        </div>
        <div className="h-1 bg-white/5 rounded-full overflow-hidden">
          <motion.div 
            initial={{ width: 0 }}
            animate={{ width: `${progress}%` }}
            className={`h-full bg-accent shadow-[0_0_10px_rgba(193,255,114,0.5)]`}
          />
        </div>
      </div>
      <button 
        onClick={onIncrement}
        className="w-10 h-10 bg-white/5 rounded-xl border border-white/5 flex items-center justify-center text-white/30 hover:bg-accent hover:text-black hover:border-accent transition-all"
      >
        <Plus className="w-4 h-4" />
      </button>
    </div>
  );
}

function Planner({ plan }: { plan: WorkoutPlan }) {
  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}
      className="p-6 space-y-8 max-w-lg mx-auto pb-32"
    >
      <header className="flex justify-between items-center border-b border-white/10 pb-6 pt-4">
        <h1 className="text-2xl font-black italic tracking-tighter uppercase tracking-widest">30-Day Plan</h1>
        <button className="p-2 bg-white/5 border border-white/5 rounded-xl text-white/40"><Settings className="w-4 h-4" /></button>
      </header>

      <div className="grid grid-cols-7 gap-2">
        {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((d, i) => (
          <div key={i} className="text-center text-[9px] font-black text-white/20 uppercase tracking-widest">{d}</div>
        ))}
        {Object.entries(plan.days).slice(0, 31).map(([date, day], i) => {
          const isToday = date === new Date().toISOString().split('T')[0];
          return (
            <div 
              key={date} 
              className={`aspect-square rounded-full flex flex-col items-center justify-center transition-all cursor-pointer border text-[10px] font-black font-mono transition-transform hover:scale-105 ${
                isToday ? 'bg-accent text-black border-accent shadow-lg shadow-accent/20' : 
                day.isCompleted ? 'bg-white/10 text-accent border-accent/30' :
                day.exercises.length === 0 ? 'bg-transparent text-white/10 border-white/5' :
                'bg-card-dark text-white/50 border-white/10 hover:border-accent'
              }`}
            >
              <span>{i + 1}</span>
            </div>
          );
        })}
      </div>

      <div className="mt-8 bg-card-dark p-10 rounded-[2.5rem] border border-white/10 overflow-hidden relative shadow-stat-glow">
        <div className="relative z-10">
          <h3 className="text-xl font-black italic uppercase tracking-tighter mb-2">Edit Schedule</h3>
          <p className="text-[11px] font-bold text-white/30 leading-relaxed uppercase tracking-wider">Linh hoạt sắp xếp. Mọi thay đổi sẽ được AI tối ưu lại.</p>
          <button className="mt-8 w-full border border-accent/40 text-accent px-6 py-4 rounded-xl text-[10px] font-black uppercase tracking-[0.2em] hover:bg-accent hover:text-black transition-all">
            Customize Routine
          </button>
        </div>
        <Calendar className="absolute -bottom-10 -right-10 w-48 h-48 text-white/5 rotate-12" />
      </div>
    </motion.div>
  );
}

function Library() {
  const [exercises] = useState([...DEFAULT_EXERCISES]);
  const [showAdd, setShowAdd] = useState(false);

  return (
    <motion.div 
      initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }}
      className="p-6 space-y-8 max-w-lg mx-auto pb-32"
    >
      <header className="flex justify-between items-center border-b border-white/10 pb-6 pt-4">
        <h1 className="text-2xl font-black italic tracking-tighter uppercase tracking-widest">Database</h1>
        <button 
          onClick={() => setShowAdd(true)}
          className="p-2 bg-accent text-black rounded-xl hover:scale-110 transition-transform"
        >
          <Plus className="w-5 h-5" />
        </button>
      </header>

      <div className="grid grid-cols-1 gap-4">
        {exercises.map(ex => (
          <div key={ex.id} className="bg-card-dark p-6 rounded-[1.5rem] border border-white/5 group hover:border-white/20 transition-all">
            <div className="flex justify-between items-start mb-4">
              <div>
                <span className="text-[9px] font-black text-accent uppercase tracking-[0.3em] bg-accent/5 px-2 py-0.5 rounded-full border border-accent/10">{ex.category}</span>
                <h3 className="text-lg font-black uppercase tracking-tight italic mt-3">{ex.name}</h3>
              </div>
              <Flame className="w-5 h-5 text-white/20 group-hover:text-accent transition-colors" />
            </div>
            <p className="text-[11px] font-medium text-white/40 leading-relaxed mb-6 uppercase tracking-wider">{ex.description}</p>
            <div className="flex gap-3">
              <div className="bg-white/5 px-3 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest text-white/60">
                {ex.reps ? `${ex.reps} Reps` : `${ex.duration}s`}
              </div>
              <div className="bg-white/5 px-3 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest text-white/60">
                {ex.sets} Sets
              </div>
              <div className="bg-white/5 px-3 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest text-accent ml-auto">
                {ex.caloriesBurned} kcal
              </div>
            </div>
          </div>
        ))}
      </div>

      {showAdd && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-end sm:items-center justify-center p-4">
           <motion.div 
             initial={{ y: "100%", opacity: 0 }} animate={{ y: 0, opacity: 1 }}
             className="bg-card-dark w-full max-w-md rounded-[2.5rem] p-8 border border-white/10 shadow-stat-glow"
           >
             <div className="w-12 h-1 bg-white/10 rounded-full mx-auto mb-10" />
             <h2 className="text-3xl font-black italic uppercase tracking-tighter mb-8 text-center">Protocol Entry</h2>
             <div className="space-y-6">
               <div>
                  <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-2 ml-1">Label</label>
                  <input placeholder="Exercise Name" className="w-full p-4 bg-white/5 rounded-xl border border-white/5 focus:ring-1 focus:ring-accent outline-none text-white font-bold" />
               </div>
               <div className="grid grid-cols-2 gap-4">
                 <div>
                    <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-2 ml-1">Sets</label>
                    <input placeholder="0" type="number" className="w-full p-4 bg-white/5 rounded-xl border border-white/5" />
                 </div>
                 <div>
                    <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-2 ml-1">Reps</label>
                    <input placeholder="0" type="number" className="w-full p-4 bg-white/5 rounded-xl border border-white/5" />
                 </div>
               </div>
               <div className="flex gap-4 pt-4">
                  <button 
                    onClick={() => setShowAdd(false)}
                    className="flex-1 py-5 border border-white/10 text-white/30 rounded-2xl font-black text-xs uppercase tracking-widest hover:bg-white/5"
                  >
                    Cancel
                  </button>
                  <button 
                    onClick={() => setShowAdd(false)}
                    className="flex-2 bg-accent text-black py-5 rounded-2xl font-black text-xs uppercase tracking-[0.2em] shadow-lg shadow-accent/10"
                  >
                    Commit
                  </button>
               </div>
             </div>
           </motion.div>
        </div>
      )}
    </motion.div>
  );
}

function NavButton({ icon: Icon, active, onClick, label }: any) {
  return (
    <button onClick={onClick} className="flex flex-col items-center gap-1.5 group">
      <div className={`p-1.5 rounded-lg transition-all ${active ? 'bg-accent/10' : 'group-hover:bg-white/5'}`}>
        <Icon className={`w-5 h-5 transition-colors ${active ? 'text-accent' : 'text-white/20'}`} />
      </div>
      <span className={`text-[9px] font-black uppercase tracking-[0.2em] transition-colors ${active ? 'text-accent' : 'text-white/20'}`}>{label}</span>
    </button>
  );
}
