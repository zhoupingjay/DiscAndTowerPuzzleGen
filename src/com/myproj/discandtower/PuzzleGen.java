package com.myproj.discandtower;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class PuzzleGen {
	public static int NumTowers = 3;
	public static int MaxStep = 64;
	private ArrayList<PuzzleState> puzzles = new ArrayList<PuzzleState>();
	private static HashSet<Integer> stateVisited = new HashSet<Integer>();
	
	public void genInitOrders(int puzzleSize, int pos, int[] order, boolean[] used) {
		if(pos == puzzleSize) {
			PuzzleState pz = new PuzzleState();
			pz.puzzleSize = puzzleSize;
			pz.initOrder = new int[puzzleSize];
			for(int i = 0; i < puzzleSize; i++) {
				// System.out.print(order[i]);
				pz.towers[0].disks.push(order[i]);
				pz.initOrder[puzzleSize - i - 1] = order[i];
			}
			// System.out.println(pz.towers[0].disks.toString());
			pz.printInitOrder();
			puzzles.add(pz);
			return;
		}
		for(int i = 0; i < puzzleSize; i++) {
			if(!used[i]) {
				order[pos] = i;
				used[i] = true;
				genInitOrders(puzzleSize, pos + 1, order, used);
				used[i] = false;
			}
		}
	}
	
	public boolean findSolution(PuzzleState state, int step) {
		if(state.win()) {
			System.out.println("Found solution at step " + step);
			state.printInitOrder();
			state.printSteps();
			return true;
		}
		if(step > MaxStep) {
			return false;
		}
		for(int from = 0; from < NumTowers; from++) {
			if(state.towers[from].disks.isEmpty()) continue;
			for(int to = 0; to < NumTowers; to++) {
				if(to == from) continue;
				if(!state.towers[to].disks.isEmpty() && state.towers[to].disks.peek() < state.towers[from].disks.peek()) {
					continue;
				}
				// try the move
				state.move(from, to);
				int curHash = state.hashCode();
				if(!stateVisited.contains(curHash)) { 
					stateVisited.add(curHash);
					if(findSolution(state, step + 1)) return true;
					stateVisited.remove(curHash);
				}
				state.undo();
			}
			
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PuzzleGen puzzleGen = new PuzzleGen();
		boolean[] used = {false, false, false, false, false};
		int[] order = new int[5];

		puzzleGen.genInitOrders(5, 0, order, used);
		System.out.println("Puzzles initialized: " + puzzleGen.puzzles.size());
		
		for(int i = 0; i < puzzleGen.puzzles.size(); i++) {
			stateVisited.clear();
			if(!puzzleGen.findSolution(puzzleGen.puzzles.get(i), 0)) {
				puzzleGen.puzzles.get(i).printInitOrder();
				System.out.println("Cannot find a solution");
			}
		}
	}

	public class PuzzleState {
		public int puzzleSize = 0;
		public int[] initOrder;
		public TowerState[] towers = new TowerState[NumTowers];
		public Stack<Move> steps = new Stack<Move>();
		
		public PuzzleState() {
			steps.clear();
			for(int i = 0; i < NumTowers; i++) {
				towers[i] = new TowerState();
				towers[i].disks.clear();
			}
		}
		public void printInitOrder() {
			for(int i = 0; i < initOrder.length; i++) {
				System.out.print(initOrder[i]);
			}
			System.out.println();
		}
		public void printSteps() {
			if(steps.isEmpty()) return;
			int numSteps = steps.size();
			for(int i = 0; i < numSteps; i++) {
				Move mv = steps.get(i);
				System.out.println("(" + mv.from + "->" + mv.to + ")");
			}
		}
		public boolean win() {
			for(int i = 0; i < NumTowers; i++) {
				if(towers[i].win(puzzleSize)) return true;
			}
			return false;
		}
		public void move(int from, int to) {
			assert(from >= 0 && from < NumTowers && to >= 0 && to < NumTowers);
			if(from == to) return;
			assert(!towers[from].disks.isEmpty());
			int disk = towers[from].disks.pop();
			towers[to].disks.push(disk);
			steps.push(new Move(from, to));
		}
		public void undo() {
			assert(!steps.isEmpty());
			Move mv = steps.pop();
			assert(!towers[mv.to].disks.isEmpty());
			int disk = towers[mv.to].disks.pop();
			towers[mv.from].disks.push(disk);
		}
		public HashSet<TowerState> getTowerStateSet() {
			HashSet<TowerState> stateSet = new HashSet<TowerState>();
			for(int i = 0; i < NumTowers; i++) {
				stateSet.add(this.towers[i]);
			}
			return stateSet;
		}
		@Override
		public int hashCode() {
			int hash = 0;
			ArrayList<Integer> towerCodes = new ArrayList<Integer>();
			for(int i = 0; i < NumTowers; i++) {
				// hash = (hash << 12) + towers[i].hashCode();
				towerCodes.add(towers[i].hashCode());
			}
			Collections.sort(towerCodes);
			for(int i = 0; i < NumTowers; i++) {
				assert(towerCodes.get(i) < 2048);
				hash = (hash << 12) + towerCodes.get(i);
			}
			return hash;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj == null) return false;
			if(!(obj instanceof PuzzleState)) return false;
			PuzzleState other = (PuzzleState) obj;
			
			HashSet<TowerState> stateSet_other = other.getTowerStateSet();
			for(int i = 0; i < NumTowers; i++) {
				if(!stateSet_other.contains(towers[i])) return false;
			}
			return true;
		}
	}
	
	public class Move {
		public int from, to;
		public Move(int from, int to) {
			this.from = from;
			this.to = to;
		}
	}
	
	public class TowerState {
		public Stack<Integer> disks = new Stack<Integer>();
		
		public boolean win(int puzzleSize) {
			if(disks.size() != puzzleSize) return false;
			for(int i = 0; i < puzzleSize; i++) {
				if(disks.get(puzzleSize - i - 1) != i) return false;
			}
			return true;
		}
		@Override
		public int hashCode() {
			int len = disks.size();
			int hash = 0;
			for(int i = 0; i < len; i++) {
				hash = (hash << 3) + (disks.get(i) + 1);
			}
			return hash;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj == null) return false;
			if(!(obj instanceof TowerState)) return false;

			TowerState other = (TowerState) obj;
			return (other.hashCode() == this.hashCode());
		}
	}
}
