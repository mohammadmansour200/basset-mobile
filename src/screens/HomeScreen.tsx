import {useTheme} from "@react-navigation/native";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {useWindowDimensions} from "react-native";
import {SceneMap, TabBar, TabView} from "react-native-tab-view";
import {useFilePathStore} from "../stores/filePathStore";
import AudioToVideo from "../tabs/AudioToVideo";
import Compress from "../tabs/Compress";
import Convert from "../tabs/Convert";
import Quality from "../tabs/Quality";
import Trim from "../tabs/Trim";
import VideoToAudio from "../tabs/VideoToAudio";
import {getIsAudio} from "../utils/getIsAudio";
import {useActiveSwipeStore} from "../stores/activeSwipeStore";

const videoRenderScene = SceneMap({
	trim: Trim,
	vidtoaud: VideoToAudio,
	convert: Convert,
	quality: Quality,
	compress: Compress,
});

const audioRenderScene = SceneMap({
	trim: Trim,
	audtovid: AudioToVideo,
	convert: Convert,
	compress: Compress,
});

export default function HomeScreen() {
	const colors = useTheme().colors;
	const layout = useWindowDimensions();
	const {inputFile} = useFilePathStore();
	const [index, setIndex] = useState(0);
	const {t} = useTranslation();

	const isAudio = getIsAudio(inputFile);

	const {activeSwipe} = useActiveSwipeStore();
	console.log(`[[p&s]]  > activeSwipe`, activeSwipe);

	const [videoRoutes] = useState([
		{key: "trim", title: t("tabs.trimTab")},
		{key: "vidtoaud", title: t("tabs.convertToAudioTab")},
		{key: "convert", title: t("tabs.convertTab")},
		{key: "quality", title: t("tabs.qualityTab")},
		{key: "compress", title: t("tabs.compressTab")},
	]);

	const [audioRoutes] = useState([
		{key: "trim", title: t("tabs.trimTab")},
		{key: "audtovid", title: t("tabs.convertToVideoTab")},
		{key: "convert", title: t("tabs.convertTab")},
		{key: "compress", title: t("tabs.compressTab")},
	]);

	const routes = isAudio ? audioRoutes : videoRoutes;

	const swipeEnabled = activeSwipe === null;

	return (
		<TabView
			navigationState={{index, routes}}
			renderScene={isAudio ? audioRenderScene : videoRenderScene}
			onIndexChange={setIndex}
			initialLayout={{width: layout.width}}
			swipeEnabled={swipeEnabled}
			renderTabBar={(props) => (
				<TabBar
					{...props}
					android_ripple={{borderless: true, color: colors.text}}
					scrollEnabled
					bounces
					labelStyle={{
						color: colors.text,
					}}
					indicatorStyle={{
						backgroundColor: colors.text,
						borderTopRightRadius: 40,
						borderTopLeftRadius: 40,
						height: 3,
					}}
					style={{
						backgroundColor: colors.background,
						borderBottomColor: colors.border,
						borderBottomWidth: 2,
					}}
				/>
			)}
		/>
	);
}
