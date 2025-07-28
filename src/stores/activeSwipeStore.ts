import {create} from "zustand";

export interface IInputFile {
	activeSwipe: string | null;
}

type State = {
	activeSwipe: "trim" | null;
};

type Action = {
	setActiveSwipe: (activeSwipe: State["activeSwipe"]) => void;
};

export const useActiveSwipeStore = create<State & Action>((set) => ({
	activeSwipe: null,
	setActiveSwipe: (activeSwipe) => set(() => ({activeSwipe: activeSwipe})),
}));
