import MultiSlider from "@ptomasroos/react-native-multi-slider";
import {useIsFocused, useTheme} from "@react-navigation/native";
import {useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {
	KeyboardAvoidingView,
	Platform,
	ScrollView,
	StyleSheet,
	Text,
	TextInput,
	View,
	useWindowDimensions,
} from "react-native";
import Video, {type VideoRef} from "react-native-video";
import {useAVStore} from "../stores/AVStore";
import {useFilePathStore} from "../stores/filePathStore";
import {getIsAudio} from "../utils/getIsAudio";
import {getPercentage} from "../utils/getPercentage";
import formatTimestamp from "../utils/timestampFormatter";
import unformatTimestamp from "../utils/timestampUnformatter";
import AVControls from "./AVControls";
import ExecuteBtn from "./ExecuteBtn";
import FileNameInput from "./FileNameInput";
import Seperator from "./Seperator";
import {useActiveSwipeStore} from "../stores/activeSwipeStore";

function AVPlayer() {
	const [isPaused, setIsPaused] = useState(true);
	const [AVCurrPosition, setAVCurrPosition] = useState(0);
	const [cutTimestamps, setCutTimestamps] = useState<[number, number]>([
		0, 100,
	]);
	const [fileName, setFileName] = useState("");
	const videoRef = useRef<VideoRef>(null);
	const {t} = useTranslation();
	const colors = useTheme().colors;
	const {setAVDuration} = useAVStore();
	const {inputFile} = useFilePathStore();
	const screenIsFocused = useIsFocused();

	const isAudio = getIsAudio(inputFile);
	return (
		<ScrollView>
			<KeyboardAvoidingView
				behavior={Platform.OS === "ios" ? "padding" : "position"}
			>
				<Video
					repeat
					onProgress={(e) => {
						setAVCurrPosition(e.currentTime);
					}}
					paused={isPaused}
					style={[styles.videoPlayer, {display: isAudio ? "none" : "flex"}]}
					ref={videoRef}
					viewType={1}
					showNotificationControls
					playInBackground={screenIsFocused}
					onLoad={(e) => setAVDuration(e.duration)}
					source={{
						uri: inputFile?.uri,
					}}
				/>
				<View style={{alignItems: "center"}}>
					<Text
						style={[
							styles.outputDuration,
							{borderColor: colors.border, color: colors.text},
						]}
					>
						{t("tabs.cutOutputDuration")}:{" "}
						{formatTimestamp(Math.max(0, cutTimestamps[1] - cutTimestamps[0]))}
					</Text>
				</View>
				<AVControls
					setAVCurrPosition={setAVCurrPosition}
					AVCurrPosition={AVCurrPosition}
					isPaused={isPaused}
					setIsPaused={setIsPaused}
					videoRef={videoRef}
				/>
				<TrimTimeline
					setAVCurrPosition={setAVCurrPosition}
					setCutTimestamps={setCutTimestamps}
					cutTimestamps={cutTimestamps}
					videoRef={videoRef}
				/>
				<FileNameInput setFileName={setFileName} />
				<ExecuteBtn
					fileName={fileName}
					btnTitle={t("executeBtn.cutBtn")}
					command={`-ss ${formatTimestamp(
						cutTimestamps[0]
					)} -to ${formatTimestamp(cutTimestamps[1])} -i ${
						inputFile?.uri
					} -c copy`}
				/>
			</KeyboardAvoidingView>
		</ScrollView>
	);
}

interface ITrimTimelineProps {
	cutTimestamps: [number, number];
	setCutTimestamps: React.Dispatch<React.SetStateAction<[number, number]>>;
	videoRef: React.RefObject<VideoRef>;
	setAVCurrPosition: React.Dispatch<React.SetStateAction<number>>;
}

const timestampRegex =
	/^(?:\d+(?::[0-5][0-9]:[0-5][0-9])?|[0-5]?[0-9]:[0-5][0-9])$/;

function TrimTimeline({
	cutTimestamps,
	setCutTimestamps,
	videoRef,
	setAVCurrPosition,
}: ITrimTimelineProps) {
	const {AVDuration} = useAVStore();
	const colors = useTheme().colors;
	const {width} = useWindowDimensions();
	const {setActiveSwipe} = useActiveSwipeStore();

	function onTimelineValuesChange(values: number[]) {
		if (!videoRef) return;

		setCutTimestamps([
			(values[0] / 100) * AVDuration,
			(values[1] / 100) * AVDuration,
		]);

		//Basically I want when the first thumb slides I want to seek the video to it, but I don't think this will be ideal if only the second thumb slides... I think it will be annoying
		if (
			getPercentage(cutTimestamps[1], AVDuration) === values[1] ||
			(getPercentage(cutTimestamps[1], AVDuration) !== values[1] &&
				getPercentage(cutTimestamps[0], AVDuration) !== values[0])
		) {
			setAVCurrPosition((values[0] / 100) * AVDuration);
			videoRef.current?.seek((values[0] / 100) * AVDuration);
		}
	}

	function onStartTimestampInputChange(value: string) {
		if (value.length <= 3) return;
		if (!videoRef) return;

		if (timestampRegex.test(value)) {
			const startCutTimestamp = unformatTimestamp(value);
			if (startCutTimestamp) {
				setCutTimestamps([startCutTimestamp, cutTimestamps[1]]);
				videoRef?.current?.seek(startCutTimestamp);
			}
		}
	}

	function onEndTimestampInputChange(value: string) {
		if (value.length <= 3) return;

		if (timestampRegex.test(value)) {
			const endCutTimestamp = unformatTimestamp(value);
			if (endCutTimestamp)
				setCutTimestamps([cutTimestamps[0], endCutTimestamp]);
		}
	}

	return (
		<View style={styles.trimTimelineContainer}>
			<Seperator
				orientation="horizontal"
				color={colors.border}
				strokeWidth={0.5}
			/>
			<MultiSlider
				onValuesChangeStart={() => {
					setActiveSwipe("trim");
				}}
				onValuesChangeFinish={() => {
					setActiveSwipe(null);
				}}
				onValuesChange={onTimelineValuesChange}
				min={0}
				max={100}
				values={
					[
						getPercentage(cutTimestamps[0], AVDuration),
						getPercentage(cutTimestamps[1], AVDuration),
					] || [0, getPercentage(100, AVDuration)]
				}
				sliderLength={width - 40}
				step={0.1}
				selectedStyle={{
					backgroundColor: colors.text,
				}}
				unselectedStyle={{
					backgroundColor: colors.border,
				}}
				markerStyle={{
					borderColor: colors.text,
					borderWidth: 3,
					backgroundColor: colors.background,
					paddingVertical: 13,
					paddingHorizontal: 5,
					top: 15,
				}}
				trackStyle={{
					paddingVertical: 15,
					borderTopLeftRadius: 5,
					borderBottomLeftRadius: 5,
					borderTopRightRadius: 5,
					borderBottomRightRadius: 5,
				}}
			/>
			<View style={styles.cutTimestampInputContainer}>
				<TextInput
					onChangeText={onStartTimestampInputChange}
					defaultValue={formatTimestamp(cutTimestamps[0])}
					cursorColor={colors.text}
					textAlign="center"
					style={[
						styles.cutTimestampInput,
						{borderColor: colors.border, color: colors.text},
					]}
				/>
				<Text style={{fontSize: 15, color: colors.text}}>:</Text>
				<TextInput
					onChangeText={onEndTimestampInputChange}
					defaultValue={formatTimestamp(cutTimestamps[1])}
					textAlign="center"
					cursorColor={colors.text}
					style={[
						styles.cutTimestampInput,
						{borderColor: colors.border, color: colors.text},
					]}
				/>
			</View>
		</View>
	);
}

const styles = StyleSheet.create({
	videoPlayer: {
		width: "100%",
		height: "auto",
		aspectRatio: "16/9",
		backgroundColor: "black",
		borderRadius: 10,
	},
	controlsContainer: {
		marginTop: 10,
		display: "flex",
		alignItems: "center",
	},
	trimTimelineContainer: {
		display: "flex",
		alignItems: "center",
		marginTop: 10,
	},
	cutTimestampInputContainer: {
		marginTop: 5,
		display: "flex",
		flexDirection: "row",
		alignItems: "center",
		gap: 2,
	},
	cutTimestampInput: {
		borderWidth: 1,
		borderRadius: 10,
		padding: 3,
	},
	outputDuration: {
		borderWidth: 1,
		paddingHorizontal: 4,
		borderRadius: 6,
		marginTop: 10,
		fontSize: 15,
	},
});

export default AVPlayer;
