-- listening_history table definition and RLS policies
CREATE TABLE IF NOT EXISTS listening_history (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES auth.users(id),
  track_id text NOT NULL,
  played_at timestamptz DEFAULT now(),
  position_ms bigint DEFAULT 0,
  duration_ms bigint DEFAULT 0,
  completed boolean DEFAULT false
);

ALTER TABLE listening_history ENABLE ROW LEVEL SECURITY;

-- Allow users to insert their own history rows
CREATE POLICY "Users can insert their own history" ON listening_history
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Allow users to read their own history rows
CREATE POLICY "Users can select their own history" ON listening_history
  FOR SELECT USING (auth.uid() = user_id);

-- Allow users to update their own history rows (progress, completion)
CREATE POLICY "Users can update their own history" ON listening_history
  FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Allow users to delete their own history rows
CREATE POLICY "Users can delete their own history" ON listening_history
  FOR DELETE USING (auth.uid() = user_id);
