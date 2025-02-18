//WebcamCapture.js
//얼굴촬영하는 페이지

import React, { useRef, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Webcam from "react-webcam";
import "./WebcamCapture.css";
import axios from "axios";

const WebcamCapture = ({ onCapture }) => {
  const navigate = useNavigate();
  const webcamRef = useRef(null);
  const [countdown, setCountdown] = useState(3);
  const [capturedImage, setCapturedImage] = useState(null);
  const [showCountdown, setShowCountdown] = useState(false); // 새로운 state 추가

  useEffect(() => {
    const webcamElement = webcamRef.current.video;
    const overlayImage = document.getElementById("person-image");
  }, []);

  const handleCaptureClick = async () => {
    setCountdown(3); // 초기화
    setShowCountdown(true); // countdown-indicator를 표시하기 위해 state 업데이트

    // 1초마다 countdown 값을 감소시키는 타이머
    const countdownTimer = setInterval(() => {
      setCountdown((prevCountdown) => prevCountdown - 1);
    }, 1000);

    // 3초 뒤에 실행되는 타이머
    setTimeout(async () => {
      clearInterval(countdownTimer); // 카운트다운 타이머 중지
      setShowCountdown(false); // countdown-indicator를 숨기기 위해 state 업데이트
      const imageSrc = webcamRef.current.getScreenshot();

      navigate("/image", {
        state: { capturedImage: imageSrc },
      });
    }, 3000); // 3초 후에 실행
  };

  return (
    <div>
      <div id="video-container">
        {/* mirrored 속성을 true로 설정하여 웹캠 피드를 좌우로 뒤집습니다. */}
        <Webcam
          audio={false}
          ref={webcamRef}
          screenshotFormat="image/jpeg"
          mirrored={true}
        />

        {/* 이미지를 겹치게 표시할 부분 */}
        <div className="overlay-image-container">
          <img id="person-image" src="/person.png" alt="Person Image" />
        </div>
      </div>
      {/* 촬영 버튼 */}
      <button className="webcam-capture-button" onClick={handleCaptureClick}>
        촬영하기
      </button>

      {/* 카운트다운 표시 */}
      {showCountdown && (
        <div className="countdown-indicator">{countdown}초 후에 촬영됩니다</div>
      )}
    </div>
  );
};

export default WebcamCapture;

// //파일 선택해서 이미지 업로드하는 용
// import React, { useRef, useState } from "react";
// import axios from "axios";
// import { useNavigate } from "react-router-dom";

// const WebcamCapture = () => {
//   const fileInputRef = useRef(null);
//   const [capturedImage, setCapturedImage] = useState(null);
//   const navigate = useNavigate();

//   const handleImageUpload = async () => {
//     const fileInput = fileInputRef.current;
//     const file = fileInput.files[0];

//     if (file) {
//       try {
//         // FormData에 이미지 추가
//         const formData = new FormData();
//         formData.append("image", file, file.name);

//         navigate("/loading", {
//           state: { capturedImage: URL.createObjectURL(file) }, // Pass the image URL to the next route
//         });

//         // 이미지를 서버로 전송
//         const uploadResponse = await axios.post(
//           "http://localhost:8000/api/upload/",
//           formData,
//           {
//             headers: {
//               "Content-Type": "multipart/form-data",
//             },
//           }
//         );

//         console.log("이미지 업로드 성공:", uploadResponse.data);

//         // 여기에서 적절한 처리를 추가하거나 다른 페이지로 이동할 수 있습니다.
//       } catch (error) {
//         // 전송 실패한 경우 오류 처리
//         console.error("이미지 업로드 실패:", error);
//       }
//     }
//   };

//   return (
//     <div>
//       {/* 파일 선택을 위한 input */}
//       <input type="file" accept="image/*" ref={fileInputRef} />

//       {/* 이미지 업로드 버튼 */}
//       <button onClick={handleImageUpload}>이미지 업로드</button>
//     </div>
//   );
// };

// export default WebcamCapture;
