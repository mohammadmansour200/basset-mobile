import {useTheme} from "@react-navigation/native";
import * as DocumentPicker from "expo-document-picker";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {Pressable, StyleSheet, Text, View} from "react-native";
import {G, Path, Svg} from "react-native-svg";
import ExecuteBtn from "../components/ExecuteBtn";
import FileNameInput from "../components/FileNameInput";
import {useFilePathStore} from "../stores/filePathStore";
import {getFileExt, getFileName} from "../utils/fileUtils";

function AudioToVideo() {
	const [imageFilePath, setImageFilePath] = useState("");
	const [fileName, setFileName] = useState("");

	const colors = useTheme().colors;

	const {inputFile} = useFilePathStore();

	const {t} = useTranslation();

	async function onUploadImageBtnClick() {
		const file = await DocumentPicker.getDocumentAsync({
			type: ["image/*"],
		});
		const selectedFile = file.assets?.[0];
		if (selectedFile) {
			setImageFilePath(selectedFile.uri);
		}
	}

	return (
		<View style={styles.container}>
			<Pressable
				android_ripple={{color: colors.text, radius: 105}}
				style={[styles.button, {backgroundColor: colors.border}]}
				onPress={onUploadImageBtnClick}
			>
				{imageFilePath === "" ? (
					<>
						<ImageIcon />
						<Text style={{fontWeight: "700", color: colors.text, fontSize: 15}}>
							{t("uploadPage.uploadFileLabel")}
						</Text>
					</>
				) : (
					<Text style={{fontWeight: "500", color: "#3b82f6"}}>
						...{getFileName(imageFilePath).slice(20)}.
						{getFileExt(imageFilePath)}
					</Text>
				)}
			</Pressable>
			<FileNameInput setFileName={setFileName} />
			<ExecuteBtn
				fileName={fileName}
				btnTitle={t("executeBtn.convertBtn")}
				disabled={imageFilePath === ""}
				outputFormat="mp4"
				command={`-r 1 -loop 1 -i ${imageFilePath} -i ${inputFile?.uri} -c:a aac -b:a 192k -r 1 -pix_fmt yuv420p -preset ultrafast -vf scale="trunc(oh*a/2)*2:720" -tune stillimage -shortest`}
			/>
		</View>
	);
}

function ImageIcon() {
	const colors = useTheme().colors;
	return (
		<Svg viewBox="0 0 16 16" fill="none" width="20" height="20">
			<G strokeWidth="0" />
			<G strokeLinecap="round" strokeLinejoin="round" />
			<G>
				<Path
					fillRule="evenodd"
					clipRule="evenodd"
					d="M2 12.4142V13C2 13.5523 2.44772 14 3 14H13C13.5523 14 14 13.5523 14 13V8.4142C14 8.149 13.8946 7.8946 13.7071 7.7071L12.7071 6.7071C12.3166 6.3166 11.6834 6.3166 11.2929 6.7071L8.7071 9.2929C8.3166 9.6834 7.6834 9.6834 7.2929 9.2929L6.7071 8.7071C6.3166 8.3166 5.68342 8.3166 5.29289 8.7071L2.29289 11.7071C2.10536 11.8946 2 12.149 2 12.4142zM4.5 6C5.32843 6 6 5.32843 6 4.5C6 3.67157 5.32843 3 4.5 3C3.67157 3 3 3.67157 3 4.5C3 5.32843 3.67157 6 4.5 6zM3 0H13C14.6569 0 16 1.34315 16 3V13C16 14.6569 14.6569 16 13 16H3C1.34315 16 0 14.6569 0 13V3C0 1.34315 1.34315 0 3 0z"
					fill={colors.text}
				/>
			</G>
		</Svg>
	);
}

const styles = StyleSheet.create({
	container: {
		alignItems: "center",
		paddingTop: 10,
	},
	button: {
		flexDirection: "row",
		gap: 3,
		paddingHorizontal: 60,
		paddingVertical: 7,
		borderRadius: 10,
	},
});

export default AudioToVideo;
