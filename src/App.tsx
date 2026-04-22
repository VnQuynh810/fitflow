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
  const [view, setView] = useState<'onboarding' | 'workout-setup' | 'workout-session' | 'dashboard' | 'planner' | 'library' | 'profile'>('onboarding');
  const [stats, setStats] = useState<UserStats | null>(null);
  const [preferences, setPreferences] = useState({
    equipment: 'bodyweight',
    intensity: 'intermediate',
    focus: ['Full Body'],
    daysPerWeek: 5
  });
  const [plan, setPlan] = useState<WorkoutPlan | null>(null);
  const [currentWorkout, setCurrentWorkout] = useState<any>(null);
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
    const savedPrefs = localStorage.getItem('fitflow_prefs');

    if (savedStats) setStats(JSON.parse(savedStats));
    if (savedPlan) setPlan(JSON.parse(savedPlan));
    if (savedActivity) setActivity(JSON.parse(savedActivity));
    if (savedStreak) setStreak(JSON.parse(savedStreak));
    if (savedPrefs) setPreferences(JSON.parse(savedPrefs));

    if (!savedStats) setView('onboarding');
  }, []);

  useEffect(() => {
    if (stats) localStorage.setItem('fitflow_stats', JSON.stringify(stats));
    if (plan) localStorage.setItem('fitflow_plan', JSON.stringify(plan));
    if (activity) localStorage.setItem('fitflow_activity', JSON.stringify(activity));
    if (streak) localStorage.setItem('fitflow_streak', JSON.stringify(streak));
    if (preferences) localStorage.setItem('fitflow_prefs', JSON.stringify(preferences));
  }, [stats, plan, activity, streak, preferences]);

  const handleCompleteOnboarding = (newStats: UserStats) => {
    setStats(newStats);
    setView('workout-setup');
  };

  const startWorkout = (dayPlan: any) => {
    setCurrentWorkout(dayPlan);
    setView('workout-session');
  };

  const handleCompleteSetup = async () => {
    if (!stats) return;
    setLoading(true);
    // Combine stats and preferences for AI prompt in a real app
    const newPlan = await generate30DayPlan(stats);
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
      <div className="w-full max-w-md h-full sm:h-[85vh] sm:rounded-[3rem] sm:border-[8px] sm:border-white/10 bg-bg-dark overflow-y-auto overflow-x-hidden relative shadow-2xl selection:bg-accent selection:text-black hide-scrollbar relative">
        {/* Dynamic Background Pattern */}
        <div className="absolute inset-0 pointer-events-none opacity-20" 
             style={{ backgroundImage: 'radial-gradient(circle at 2px 2px, rgba(255,255,255,0.05) 1px, transparent 0)', backgroundSize: '24px 24px' }} />
        
        <AnimatePresence mode="wait">
          {view === 'onboarding' && (
            <Onboarding 
              onComplete={handleCompleteOnboarding} 
              isLoading={loading} 
            />
          )}

          {view === 'workout-setup' && (
            <WorkoutSetup 
              preferences={preferences}
              setPreferences={setPreferences}
              onComplete={handleCompleteSetup}
              isLoading={loading}
            />
          )}

          {view === 'workout-session' && currentWorkout && (
            <WorkoutSession 
              workout={currentWorkout} 
              onComplete={() => {
                updateStreak();
                setView('dashboard');
              }}
              onExit={() => setView('dashboard')}
            />
          )}

          {view === 'dashboard' && stats && plan && (
            <Dashboard 
              stats={stats} 
              plan={plan} 
              activity={activity} 
              setActivity={setActivity}
              streak={streak}
              onStartWorkout={() => {
                const today = new Date().toISOString().split('T')[0];
                startWorkout(plan.days[today] || Object.values(plan.days)[0]);
              }}
            />
          )}

          {view === 'planner' && plan && (
            <Planner plan={plan} onStartWorkout={startWorkout} />
          )}

          {view === 'library' && (
            <Library />
          )}

          {view === 'profile' && stats && (
            <Profile stats={stats} onRestart={() => setView('onboarding')} />
          )}
        </AnimatePresence>

        {/* Bottom Navigation */}
        {view !== 'onboarding' && (
          <nav className="sticky bottom-0 left-0 right-0 bg-card-dark/80 backdrop-blur-xl border-t border-white/10 px-6 py-4 flex justify-between items-center z-50">
            <NavButton icon={Activity} active={view === 'dashboard'} onClick={() => setView('dashboard')} label="Home" />
            <NavButton icon={Calendar} active={view === 'planner'} onClick={() => setView('planner')} label="Plan" />
            <div 
              onClick={() => setView('workout-setup')}
              className="bg-accent p-3 rounded-full -mt-12 shadow-lg shadow-accent/20 cursor-pointer hover:scale-110 active:scale-90 transition-all flex items-center justify-center group"
            >
              <Plus className="text-black w-6 h-6" />
              <div className="absolute -top-10 scale-0 group-hover:scale-100 transition-all bg-accent text-black text-[8px] font-black px-2 py-1 rounded uppercase tracking-tighter">New Setup</div>
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

function WorkoutSession({ workout, onComplete, onExit }: any) {
  const [currentIdx, setCurrentIdx] = useState(0);
  const [timeLeft, setTimeLeft] = useState(30);
  const [isActive, setIsActive] = useState(true);
  
  const currentEx = workout.exercises[currentIdx];

  useEffect(() => {
    let timer: any;
    if (isActive && timeLeft > 0) {
      timer = setInterval(() => setTimeLeft(prev => prev - 1), 1000);
    } else if (timeLeft === 0) {
      if (currentIdx < workout.exercises.length - 1) {
        // Auto next set logic could go here
        setIsActive(false);
      }
    }
    return () => clearInterval(timer);
  }, [isActive, timeLeft, currentIdx, workout.exercises.length]);

  const handleNext = () => {
    if (currentIdx < workout.exercises.length - 1) {
      setCurrentIdx(currentIdx + 1);
      setTimeLeft(30);
      setIsActive(true);
    } else {
      onComplete();
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="max-w-md mx-auto h-full flex flex-col bg-bg-dark text-white relative"
    >
      {/* Header */}
      <div className="p-6 flex justify-between items-center bg-card-dark/50 backdrop-blur-md border-b border-white/5 relative z-20">
        <div>
          <h2 className="text-[10px] font-black text-accent uppercase tracking-[0.3em]">Session Active</h2>
          <h1 className="text-xl font-black italic tracking-tighter uppercase whitespace-nowrap overflow-hidden text-ellipsis max-w-[200px]">
            {currentEx?.name || 'Workout'}
          </h1>
        </div>
        <button onClick={onExit} className="p-3 bg-white/5 border border-white/5 rounded-full text-white/30 hover:bg-white/10 transition-colors">
          <Plus className="w-5 h-5 rotate-45" />
        </button>
      </div>

      {/* Animation Area (WebView Ready) */}
      <div className="flex-1 flex flex-col items-center justify-center p-6 relative overflow-hidden">
        {/* Decorative Grid for Animation Space */}
        <div className="absolute inset-0 opacity-10 pointer-events-none" 
             style={{ backgroundImage: 'linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px)', backgroundSize: '40px 40px' }} />
        
        {/* Conceptual Animation Placeholder */}
        <div className="w-64 h-64 relative">
          {/* Outer Ring */}
          <div className="absolute inset-0 border-[10px] border-white/5 rounded-full" />
          {/* Progress Ring */}
          <svg className="w-full h-full rotate-[-90deg]">
             <circle 
                cx="128" cy="128" r="118" 
                fill="transparent" 
                stroke="currentColor" 
                strokeWidth="10" 
                strokeDasharray="741"
                strokeDashoffset={741 - (741 * timeLeft) / 30}
                className="text-accent transition-all duration-1000 ease-linear"
             />
          </svg>
          
          {/* Inner Content: Timer & Animation Simulation */}
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <motion.div 
               animate={isActive ? { scale: [1, 1.05, 1], opacity: [0.8, 1, 0.8] } : {}}
               transition={{ duration: 2, repeat: Infinity }}
               className="w-32 h-32 bg-accent/10 rounded-full blur-xl absolute" 
            />
            <div className="text-6xl font-black italic tracking-tighter text-white z-10">{timeLeft}</div>
            <div className="text-[10px] font-black uppercase tracking-[0.3em] text-accent mt-2 z-10">Seconds</div>
          </div>
        </div>

        {/* Reps/Sets Info */}
        <div className="mt-12 text-center space-y-2">
          <div className="text-4xl font-black italic text-secondary tracking-tighter uppercase leading-none">
            {currentEx?.sets} Sets <span className="text-white/20 not-italic">×</span> {currentEx?.reps || currentEx?.duration} reps
          </div>
          <div className="text-[9px] font-black text-white/30 uppercase tracking-[0.4em]">Intensity Vector Alpha</div>
        </div>
      </div>

      {/* Footer Controls */}
      <div className="p-8 bg-card-dark/80 backdrop-blur-2xl border-t border-white/5 space-y-6">
        <div className="flex justify-between items-center">
          <div className="text-[10px] font-black text-white/20 uppercase tracking-[0.2em]">Next: <span className="text-white/60">{workout.exercises[currentIdx + 1]?.name || 'Finish'}</span></div>
          <div className="text-[10px] font-black text-secondary uppercase tracking-[0.2em]">{currentIdx + 1} / {workout.exercises.length}</div>
        </div>
        
        <div className="flex gap-3">
          <button 
            onClick={() => setIsActive(!isActive)}
            className={`flex-1 p-5 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] transition-all border ${
              isActive ? 'bg-white/5 border-white/10 text-white' : 'bg-secondary border-secondary text-black shadow-blue-glow shadow-secondary/20'
            }`}
          >
            {isActive ? 'Pause Protocol' : 'Resume Protocol'}
          </button>
          <button 
            onClick={handleNext}
            className="flex-1 bg-accent text-black p-5 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-stat-glow shadow-accent/20 hover:scale-[1.02] active:scale-95 transition-all"
          >
            {currentIdx === workout.exercises.length - 1 ? 'Finish Cycle' : 'Next Vector'}
          </button>
        </div>
      </div>
    </motion.div>
  );
}

function WorkoutSetup({ preferences, setPreferences, onComplete, isLoading }: any) {
  const toggleFocus = (f: string) => {
    if (preferences.focus.includes(f)) {
      setPreferences({ ...preferences, focus: preferences.focus.filter((item: string) => item !== f) });
    } else {
      setPreferences({ ...preferences, focus: [...preferences.focus, f] });
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}
      className="max-w-md mx-auto p-8 pt-16 pb-20"
    >
      <header className="mb-14 text-center">
        <h2 className="text-[10px] font-black text-white/30 uppercase tracking-[0.3em] mb-2">Manifest Generation</h2>
        <h1 className="text-4xl font-black italic tracking-tighter uppercase leading-none">Workout <span className="text-accent italic underline decoration-accent/30 decoration-4">Setup</span></h1>
      </header>

      <div className="space-y-10">
        <section>
          <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-6 ml-1">Equipment Level</label>
          <div className="grid grid-cols-1 gap-3">
            {[
              { id: 'bodyweight', label: 'Bodyweight Only', desc: 'No equipment required', color: 'border-secondary/20' },
              { id: 'minimal', label: 'Minimalist', desc: 'Dumbbells & Resistance bands', color: 'border-accent/20' },
              { id: 'gym', label: 'Full Protocol', desc: 'Complete high-end gym access', color: 'border-white/5' }
            ].map((eq) => (
              <button
                key={eq.id}
                onClick={() => setPreferences({ ...preferences, equipment: eq.id })}
                className={`p-5 rounded-2xl text-left transition-all border outline-none relative overflow-hidden ${
                  preferences.equipment === eq.id 
                    ? 'bg-accent border-accent text-black scale-[1.02] shadow-lg shadow-accent/10' 
                    : `bg-card-dark ${eq.color} text-white/60 hover:bg-white/5`
                }`}
              >
                <div className="text-[11px] font-black uppercase tracking-widest mb-1 relative z-10">{eq.label}</div>
                <div className={`text-[9px] font-medium uppercase tracking-wider relative z-10 ${preferences.equipment === eq.id ? 'text-black/60' : 'text-white/20'}`}>{eq.desc}</div>
                {preferences.equipment === eq.id && (
                  <div className="absolute top-0 right-0 p-2 opacity-20">
                     <Plus className="w-12 h-12 rotate-45" />
                  </div>
                )}
              </button>
            ))}
          </div>
        </section>

        <section>
          <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-6 ml-1">Target Vectors</label>
          <div className="grid grid-cols-2 gap-3">
            {['Full Body', 'Chest/Back', 'Legs', 'Core', 'Cardio Focus'].map((f) => (
              <button
                key={f}
                onClick={() => toggleFocus(f)}
                className={`p-4 rounded-xl text-[10px] font-bold uppercase tracking-wider transition-all border ${
                  preferences.focus.includes(f) 
                    ? 'bg-accent/20 border-accent/40 text-accent' 
                    : 'bg-white/5 border-white/5 text-white/30 hover:bg-white/10'
                }`}
              >
                {f}
              </button>
            ))}
          </div>
        </section>

        <section>
          <div className="flex justify-between items-end mb-4 ml-1">
            <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em]">Frequency</label>
            <div className="text-xl font-black italic text-accent">{preferences.daysPerWeek} <span className="text-[10px] text-white/20 uppercase tracking-widest not-italic">Days / Week</span></div>
          </div>
          <input 
            type="range" min="1" max="7" 
            value={preferences.daysPerWeek} 
            onChange={(e) => setPreferences({...preferences, daysPerWeek: parseInt(e.target.value)})}
            className="w-full h-1 bg-white/5 rounded-full appearance-none cursor-pointer accent-accent"
          />
        </section>

        <button
          disabled={isLoading}
          onClick={onComplete}
          className="w-full bg-accent text-black p-6 rounded-[2.2rem] font-black text-[11px] uppercase tracking-[0.4em] mt-12 flex items-center justify-center gap-3 hover:shadow-[0_15px_40px_rgba(255,95,7,0.3)] shadow-stat-glow active:scale-95 transition-all disabled:opacity-50"
        >
          {isLoading ? (
            <div className="flex items-center gap-3">
              <div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin" />
              <span>Synthesizing...</span>
            </div>
          ) : (
            <>Finalize Protocol <ChevronRight className="w-5 h-5" /></>
          )}
        </button>
      </div>
    </motion.div>
  );
}

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
      initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 1.05 }}
      className="max-w-md mx-auto p-8 pt-16 pb-20 relative overflow-hidden"
    >
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-accent/10 rounded-full blur-[120px] -z-10" />
      
      <header className="mb-14 text-center">
        <h1 className="text-5xl font-black tracking-tighter mb-4 italic uppercase leading-none">Fit<span className="text-accent italic drop-shadow-[0_0_15px_rgba(255,95,7,0.4)]">Flow</span></h1>
        <p className="text-[9px] uppercase font-bold tracking-[0.4em] text-white/30 border-t border-white/5 pt-4 inline-block">Physical Calibration Module</p>
      </header>

      <div className="space-y-12">
        <section className="relative">
          <div className="flex justify-between items-end mb-6">
            <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em]">Vertical Scale</label>
            <div className="flex items-baseline gap-1">
              <span className="text-4xl font-black italic text-white tracking-tighter leading-none">{formData.height}</span>
              <span className="text-[10px] font-bold text-white/20 uppercase tracking-widest">cm</span>
            </div>
          </div>
          <div className="relative h-12 flex items-center">
            <input 
              type="range" min="120" max="220" 
              value={formData.height} 
              onChange={(e) => setFormData({...formData, height: parseInt(e.target.value)})}
              className="w-full h-1 bg-white/5 rounded-full appearance-none cursor-pointer accent-accent relative z-10"
            />
            <div className="absolute inset-0 flex justify-between items-center px-1 pointer-events-none">
               {[...Array(6)].map((_, i) => <div key={i} className="w-0.5 h-3 bg-white/10 rounded-full" />)}
            </div>
          </div>
        </section>

        <section>
          <label className="block text-[10px] font-black text-white/20 uppercase tracking-[0.3em] mb-4 ml-1">Mass Density</label>
          <div className="relative">
            <input 
              type="number" 
              value={formData.weight} 
              onChange={(e) => setFormData({...formData, weight: parseInt(e.target.value)})}
              className="w-full p-8 bg-card-dark border border-white/5 rounded-[2.5rem] text-center text-6xl font-black italic focus:ring-1 focus:ring-accent/50 outline-none shadow-stat-glow placeholder:text-white/5 group transition-all"
            />
            <span className="absolute right-8 top-1/2 -translate-y-1/2 text-xs font-black text-white/10 tracking-widest">KG</span>
          </div>
        </section>

        <div className="bg-card-dark p-10 rounded-[3rem] text-center border border-accent/10 shadow-[0_0_50px_rgba(255,95,7,0.05)] relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-32 h-1 bg-gradient-to-l from-accent to-transparent" />
          <div className="absolute bottom-0 left-0 w-32 h-1 bg-gradient-to-r from-secondary to-transparent" />
          <div className="text-[10px] font-black text-accent/40 uppercase tracking-[0.4em] mb-4">Body Mass Index</div>
          <div className="text-6xl font-black italic mb-4 tracking-tighter text-white drop-shadow-[0_0_10px_rgba(255,255,255,0.1)]">{bmi.toFixed(1)}</div>
          <div className="inline-block px-4 py-1.5 bg-accent/20 border border-accent/20 rounded-full text-accent text-[10px] font-black uppercase tracking-[0.2em]">
            {bmi < 18.5 ? 'Deficit' : bmi < 25 ? 'Optimal' : bmi < 30 ? 'Surplus' : 'Critical'}
          </div>
          <div className="absolute bottom-0 right-0 w-24 h-24 bg-accent/10 blur-3xl rounded-full translate-x-12 translate-y-12" />
        </div>

        <section>
          <label className="block text-[10px] font-black text-white/20 uppercase tracking-[0.3em] mb-6 ml-1">Objective Vector</label>
          <div className="grid grid-cols-2 gap-4">
            {['Weight Loss', 'Muscle Gain', 'Endurance', 'Maintenance'].map((goal) => (
              <button
                key={goal}
                onClick={() => setFormData({...formData, goal: goal as any})}
                className={`p-6 rounded-2xl text-[9px] font-black uppercase tracking-[0.2em] transition-all relative overflow-hidden ${
                  formData.goal === goal 
                    ? 'bg-accent text-black shadow-[0_10px_30px_rgba(255,95,7,0.2)] scale-105 z-10' 
                    : 'bg-white/5 border border-white/5 text-white/40 hover:bg-white/10 hover:border-white/10'
                }`}
              >
                {goal}
                {formData.goal === goal && (
                   <motion.div layoutId="goal-glow" className="absolute inset-0 bg-white/20 blur-xl mix-blend-overlay" />
                )}
              </button>
            ))}
          </div>
        </section>

        <button
          disabled={isLoading}
          onClick={() => onComplete({...formData, bmi})}
          className="w-full bg-accent text-black p-6 rounded-[2.2rem] font-black text-[11px] uppercase tracking-[0.4em] mt-8 flex items-center justify-center gap-3 hover:shadow-[0_15px_40px_rgba(255,95,7,0.3)] hover:-translate-y-1 active:scale-95 active:translate-y-0 transition-all disabled:opacity-50 group"
        >
          {isLoading ? (
            <div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin" />
          ) : (
            <>Initialize Sequence <ChevronRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" /></>
          )}
        </button>
      </div>
      
      <div className="mt-20 text-center">
         <p className="text-[10px] font-bold text-white/10 uppercase tracking-[0.5em]">System Version 1.0.4 - Zero Protocol</p>
      </div>
    </motion.div>
  );
}

function Dashboard({ stats, plan, activity, setActivity, streak, onStartWorkout }: { 
  stats: UserStats, 
  plan: WorkoutPlan, 
  activity: DailyActivity,
  setActivity: (activity: DailyActivity) => void,
  streak: Streak,
  onStartWorkout: () => void
}) {
  const today = new Date().toISOString().split('T')[0];
  const todayWorkout = plan.days[today];

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }}
      className="p-6 space-y-8 max-w-lg mx-auto pb-32"
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

      {/* Hero CTA: Start Session */}
      <section>
        <button 
          onClick={onStartWorkout}
          className="w-full bg-accent text-black p-8 rounded-[2.5rem] flex items-center justify-between shadow-stat-glow group hover:scale-[1.02] transition-all relative overflow-hidden"
        >
          <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 blur-[60px] rounded-full translate-x-10 -translate-y-10" />
          <div className="text-left relative z-10">
            <h3 className="text-2xl font-black italic uppercase tracking-tighter leading-none">Initialize<br/>Protocol</h3>
            <p className="text-[10px] font-bold opacity-60 uppercase tracking-widest mt-2">{todayWorkout ? `Today: ${todayWorkout.exercises[0]?.name}` : 'Ready for action'}</p>
          </div>
          <div className="w-16 h-16 bg-black rounded-full flex items-center justify-center group-hover:rotate-45 transition-transform z-10 shadow-xl">
             <Activity className="text-accent w-8 h-8" />
          </div>
        </button>
      </section>

      {/* Streak & Stats Summary */}
        <section className="grid grid-cols-2 gap-4">
          <div className="bg-card-dark p-6 rounded-[2rem] border border-white/5 shadow-stat-glow flex flex-col justify-between h-40 relative overflow-hidden">
            <div className="absolute top-0 left-0 w-1 h-full bg-accent" />
            <Trophy className="w-6 h-6 text-accent opacity-50" />
            <div className="relative z-10">
              <div className="text-5xl font-black italic tracking-tighter text-accent">{streak.current}</div>
              <div className="text-[10px] font-bold uppercase tracking-widest text-white/40">Chuỗi Streak</div>
            </div>
          </div>
          <div className="bg-card-dark p-6 rounded-[2rem] border border-white/5 flex flex-col justify-between h-40 relative overflow-hidden shadow-blue-glow">
            <div className="absolute top-0 right-0 w-1 h-full bg-secondary" />
            <Activity className="w-6 h-6 text-secondary opacity-50" />
            <div className="relative z-10">
              <div className="text-xl font-bold uppercase tracking-tight italic text-secondary">{stats.goal}</div>
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
            color="text-secondary" 
            bgColor="bg-secondary/5"
            label="Water" 
            value={activity.waterIntake} 
            goal={WATER_GOAL} 
            unit="ml"
            barColor="bg-secondary"
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

function MetricCard({ icon: Icon, color, bgColor, label, value, goal, unit, onIncrement, barColor = "bg-accent" }: any) {
  const progress = Math.min((value / goal) * 100, 100);
  return (
    <div className="bg-card-dark p-5 rounded-[1.5rem] border border-white/5 flex items-center gap-6 shadow-sm overflow-hidden relative">
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
            className={`h-full ${barColor} shadow-[0_0_10px_rgba(255,255,255,0.2)]`}
          />
        </div>
      </div>
      <button 
        onClick={onIncrement}
        className={`w-10 h-10 bg-white/5 rounded-xl border border-white/5 flex items-center justify-center text-white/30 hover:${barColor} hover:text-black hover:border-transparent transition-all active:scale-90`}
      >
        <Plus className="w-4 h-4" />
      </button>
    </div>
  );
}

function Planner({ plan, onStartWorkout }: { plan: WorkoutPlan, onStartWorkout: (dayPlan: any) => void }) {
  const daysArray = Object.entries(plan.days).slice(0, 30);
  const weeks = [];
  for (let i = 0; i < daysArray.length; i += 7) {
    weeks.push(daysArray.slice(i, i + 7));
  }

  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}
      className="p-6 space-y-12 max-w-lg mx-auto pb-40"
    >
      <header className="flex justify-between items-center border-b border-white/10 pb-8 pt-4">
        <div>
          <h2 className="text-[10px] font-black text-white/30 uppercase tracking-[0.4em] mb-2">Master Manifest</h2>
          <h1 className="text-4xl font-black italic tracking-tighter uppercase leading-none">Monthly <span className="text-accent italic">Timeline</span></h1>
        </div>
        <button className="p-3 bg-white/5 border border-white/5 rounded-2xl text-white/40 hover:bg-white/10 transition-colors"><Settings className="w-5 h-5" /></button>
      </header>

      <div className="space-y-16">
        {weeks.map((week, wIndex) => (
          <div key={wIndex} className="space-y-6">
            <div className="flex items-center gap-4">
              <h2 className="text-sm font-black italic text-accent uppercase tracking-[0.2em] whitespace-nowrap">Week {(wIndex + 1).toString().padStart(2, '0')}</h2>
              <div className="h-px bg-white/5 w-full" />
            </div>
            
            <div className="space-y-4">
              {week.map(([date, day], dIndex) => {
                const dayNum = wIndex * 7 + dIndex + 1;
                const isToday = date === new Date().toISOString().split('T')[0];
                
                return (
                  <div 
                    key={date} 
                    onClick={() => day.exercises.length > 0 && onStartWorkout(day)}
                    className={`relative p-5 rounded-[2rem] border transition-all ${
                    isToday ? 'bg-accent/5 border-accent shadow-stat-glow cursor-pointer hover:scale-[1.02]' : 
                    day.exercises.length > 0 ? 'bg-card-dark border-white/5 hover:border-accent/30 cursor-pointer hover:scale-[1.01]' :
                    'bg-card-dark border-white/5 opacity-50'
                  }`}>
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-4">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center font-black italic text-xs ${
                          isToday ? 'bg-accent text-black shadow-[0_0_15px_rgba(255,95,7,0.4)]' : 'bg-white/5 text-white/40 border border-white/5'
                        }`}>
                          {dayNum}
                        </div>
                        <div>
                          <h3 className={`text-[10px] font-black uppercase tracking-[0.2em] ${isToday ? 'text-accent' : 'text-white/20'}`}>Day {dayNum}</h3>
                          <p className={`text-xs font-bold uppercase tracking-tight italic ${isToday ? 'text-white' : 'text-white/60'}`}>
                            {day.exercises.length > 0 ? (isToday ? 'Current Session' : 'Scheduled Activity') : 'Rest & Recovery'}
                          </p>
                        </div>
                      </div>
                      {day.isCompleted && <CheckCircle2 className="w-5 h-5 text-accent" />}
                    </div>

                    {day.exercises.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {day.exercises.map((ex, i) => (
                          <div key={i} className="bg-white/5 px-3 py-1.5 rounded-xl border border-white/5 flex items-center gap-2 group/ex hover:border-secondary/30 transition-colors">
                            <span className="text-[9px] font-black text-white/60 uppercase tracking-widest leading-none group-hover/ex:text-secondary transition-colors">{ex.name}</span>
                            <span className="text-[8px] font-bold text-accent uppercase tracking-tighter opacity-60 italic">{ex.sets}x{ex.reps || ex.duration}</span>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="bg-secondary/5 py-3 px-4 rounded-xl border border-secondary/10 border-dashed">
                        <span className="text-[9px] font-bold text-secondary uppercase tracking-[0.3em] italic">System Diagnostics & Reset</span>
                      </div>
                    )}
                    
                    {isToday && (
                      <div className="absolute top-2 right-6">
                        <span className="text-[8px] font-black px-2 py-0.5 bg-accent text-black rounded-full uppercase tracking-tighter animate-pulse">Live</span>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="bg-accent text-black p-8 rounded-[2.5rem] shadow-stat-glow flex items-center justify-between group cursor-pointer hover:scale-[1.02] transition-all">
        <div>
          <h3 className="text-xl font-black italic uppercase tracking-tighter">New Cycle</h3>
          <p className="text-[10px] font-bold opacity-60 uppercase tracking-widest mt-1">Re-generate entire logic</p>
        </div>
        <Plus className="w-8 h-8 group-hover:rotate-90 transition-transform duration-500" />
      </div>
    </motion.div>
  );
}

function Library() {
  const [exercises, setExercises] = useState([...DEFAULT_EXERCISES]);
  const [showAdd, setShowAdd] = useState(false);
  const [newEx, setNewEx] = useState({ name: '', sets: '', reps: '' });

  const handleAdd = () => {
    if (!newEx.name) return;
    setExercises([{
       id: 'custom-' + Date.now(),
       name: newEx.name.toUpperCase(),
       category: 'CUSTOM',
       description: 'USER DEFINED PROTOCOL',
       reps: parseInt(newEx.reps) || 0,
       sets: parseInt(newEx.sets) || 0,
       duration: null,
       caloriesBurned: 50
    }, ...exercises]);
    setShowAdd(false);
    setNewEx({ name: '', sets: '', reps: '' });
  };

  const removeEx = (id: string) => {
    setExercises(exercises.filter(ex => ex.id !== id));
  };

  return (
    <motion.div 
      initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }}
      className="p-6 space-y-8 max-w-lg mx-auto pb-32"
    >
      <header className="flex justify-between items-center border-b border-white/10 pb-6 pt-4">
        <div>
          <h2 className="text-[10px] font-black text-white/30 uppercase tracking-[0.3em] mb-1">Resource</h2>
          <h1 className="text-2xl font-black italic tracking-tighter uppercase tracking-widest">Database</h1>
        </div>
        <button 
          onClick={() => setShowAdd(true)}
          className="p-3 bg-accent text-black rounded-2xl shadow-lg shadow-accent/20 hover:scale-110 active:scale-95 transition-all"
        >
          <Plus className="w-5 h-5" />
        </button>
      </header>

      <div className="grid grid-cols-1 gap-4">
        {exercises.map(ex => (
           <div key={ex.id} className="bg-card-dark p-6 rounded-[2rem] border border-white/5 group hover:border-white/20 transition-all shadow-sm relative overflow-hidden">
             <div className="flex justify-between items-start mb-4">
               <div>
                  <span className="text-[9px] font-black text-accent uppercase tracking-[0.3em] bg-accent/5 px-2 py-0.5 rounded-full border border-accent/10">{ex.category}</span>
                  <h3 className="text-lg font-black uppercase tracking-tight italic mt-3">{ex.name}</h3>
               </div>
               <div className="flex gap-2">
                 <button 
                   onClick={() => removeEx(ex.id)}
                   className="p-2 bg-white/5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-500/20 text-white/20 hover:text-red-500"
                 >
                   <Flame className="w-4 h-4" />
                 </button>
               </div>
             </div>
             <p className="text-[11px] font-medium text-white/30 leading-relaxed mb-6 uppercase tracking-wider">{ex.description}</p>
             <div className="flex gap-2">
               <div className="bg-white/5 px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-widest text-white/50 border border-white/5">
                  {ex.reps ? `${ex.reps} Reps` : `${ex.duration}s`}
               </div>
               <div className="bg-white/5 px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-widest text-white/50 border border-white/5">
                  {ex.sets} Sets
               </div>
               <div className="bg-accent/5 px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-widest text-accent border border-accent/10 ml-auto">
                  {ex.caloriesBurned} kcal
               </div>
             </div>
             <div className="absolute top-0 right-0 w-24 h-24 bg-white/5 rotate-45 translate-x-12 -translate-y-12 blur-2xl" />
           </div>
        ))}
      </div>

      {showAdd && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-end sm:items-center justify-center p-4">
           <motion.div 
             initial={{ y: "100%", opacity: 0 }} animate={{ y: 0, opacity: 1 }}
             className="bg-card-dark w-full max-w-md rounded-[3rem] p-10 border border-white/10 shadow-stat-glow relative overflow-hidden"
           >
             <div className="w-12 h-1 bg-white/10 rounded-full mx-auto mb-10" />
             <h2 className="text-4xl font-black italic uppercase tracking-tighter mb-10 text-center leading-none">New <span className="text-accent underline decoration-accent/30 underline-offset-4">Entry</span></h2>
             
             <div className="space-y-8">
               <div>
                  <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-3 ml-1">Asset Designation</label>
                  <input 
                    value={newEx.name}
                    onChange={(e) => setNewEx({...newEx, name: e.target.value})}
                    placeholder="EXERCISE NAME" 
                    className="w-full p-5 bg-white/5 rounded-2xl border border-white/5 focus:ring-1 focus:ring-accent outline-none text-white font-black tracking-widest placeholder:text-white/10" 
                  />
               </div>
               <div className="grid grid-cols-2 gap-4">
                 <div>
                    <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-3 ml-1">Volume (Sets)</label>
                    <input 
                      value={newEx.sets}
                      onChange={(e) => setNewEx({...newEx, sets: e.target.value})}
                      placeholder="0" type="number" className="w-full p-5 bg-white/5 rounded-2xl border border-white/5 outline-none text-white font-black" 
                    />
                 </div>
                 <div>
                    <label className="text-[10px] font-black text-white/20 uppercase tracking-[0.3em] block mb-3 ml-1">Intensity (Reps)</label>
                    <input 
                      value={newEx.reps}
                      onChange={(e) => setNewEx({...newEx, reps: e.target.value})}
                      placeholder="0" type="number" className="w-full p-5 bg-white/5 rounded-2xl border border-white/5 outline-none text-white font-black" 
                    />
                 </div>
               </div>
               <div className="flex gap-4 pt-4">
                  <button 
                    onClick={() => setShowAdd(false)}
                    className="flex-1 py-5 border border-white/10 text-white/30 rounded-[2rem] font-black text-[10px] uppercase tracking-widest hover:bg-white/5 active:scale-95 transition-all"
                  >
                    Discard
                  </button>
                  <button 
                    onClick={handleAdd}
                    className="flex-2 bg-accent text-black py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-accent/10 px-10 active:scale-95 transition-all"
                  >
                    Initialize
                  </button>
               </div>
             </div>
             <div className="absolute -bottom-20 -left-20 w-64 h-64 bg-accent/5 rounded-full blur-3xl" />
           </motion.div>
        </div>
      )}
    </motion.div>
  );
}

function Profile({ stats, onRestart }: { stats: UserStats, onRestart: () => void }) {
  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}
      className="p-6 space-y-8 max-w-lg mx-auto pb-32"
    >
      <header className="flex justify-between items-center border-b border-white/10 pb-6 pt-4">
        <div>
          <h2 className="text-[10px] font-black text-white/30 uppercase tracking-[0.3em] mb-1">Identity</h2>
          <h1 className="text-3xl font-black italic tracking-tighter uppercase">Subject <span className="text-accent italic">Zero</span></h1>
        </div>
        <button className="p-3 bg-white/5 border border-white/5 rounded-2xl text-white/40 hover:bg-white/10 transition-colors">
          <Settings className="w-5 h-5" />
        </button>
      </header>

      <section className="bg-card-dark p-6 rounded-[2rem] border border-white/5 shadow-stat-glow relative overflow-hidden">
        <div className="relative z-10 grid grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-[10px] font-black text-white/30 uppercase tracking-[0.2em] mb-2">Weight</div>
            <div className="text-2xl font-black italic text-white">{stats.weight}<span className="text-xs text-white/40 ml-1">kg</span></div>
          </div>
          <div className="border-x border-white/5">
            <div className="text-[10px] font-black text-white/30 uppercase tracking-[0.2em] mb-2">Height</div>
            <div className="text-2xl font-black italic text-white">{stats.height}<span className="text-xs text-white/40 ml-1">cm</span></div>
          </div>
          <div>
            <div className="text-[10px] font-black text-white/30 uppercase tracking-[0.2em] mb-2">BMI</div>
            <div className="text-2xl font-black italic text-accent">
              {(stats.weight / ((stats.height / 100) ** 2)).toFixed(1)}
            </div>
          </div>
        </div>
        <div className="absolute top-0 right-0 w-32 h-32 bg-accent/5 rounded-full blur-3xl -mr-10 -mt-10" />
      </section>

      <section className="space-y-3">
        <h3 className="text-[11px] font-bold uppercase tracking-[0.3em] text-white/30 ml-1 mb-4">Operations</h3>
        
        <button 
          onClick={onRestart}
          className="w-full bg-white/5 p-5 rounded-[1.5rem] border border-white/5 flex items-center justify-between group hover:border-accent/40 transition-all"
        >
          <div className="flex items-center gap-4">
            <div className="bg-accent/10 p-3 rounded-xl">
              <Activity className="w-5 h-5 text-accent" />
            </div>
            <div className="text-left">
              <div className="text-sm font-black uppercase tracking-wider text-white group-hover:text-accent transition-colors">Re-Calibrate Body Stats</div>
              <div className="text-[10px] font-medium text-white/40 uppercase tracking-widest mt-1">Review your onboarding setup</div>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-white/20 group-hover:text-accent" />
        </button>

        <button className="w-full bg-white/5 p-5 rounded-[1.5rem] border border-white/5 flex items-center justify-between group hover:border-white/20 transition-all opacity-50 cursor-not-allowed">
          <div className="flex items-center gap-4">
            <div className="bg-white/5 p-3 rounded-xl border border-white/5">
              <Bell className="w-5 h-5 text-white/40" />
            </div>
            <div className="text-left">
              <div className="text-sm font-black uppercase tracking-wider text-white">Notifications</div>
              <div className="text-[10px] font-medium text-white/40 uppercase tracking-widest mt-1">System alerts & reminders</div>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-white/20" />
        </button>
      </section>
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
