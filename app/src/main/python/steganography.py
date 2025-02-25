import cv2
import numpy as np
from skimage.metrics import peak_signal_noise_ratio as psnr, structural_similarity as ssim

def encode_image(base_img_path, hidden_img_path, output_path):
    base_img = cv2.imread(base_img_path)
    hidden_img = cv2.imread(hidden_img_path)

    if base_img is None or hidden_img is None:
        return "Error: One or both images could not be loaded."

    base_h, base_w, _ = base_img.shape
    hidden_h, hidden_w, _ = hidden_img.shape

    if hidden_h > base_h or hidden_w > base_w:
        return "Error: Hidden image must be smaller than or equal to base image."

    hidden_resized = cv2.resize(hidden_img, (base_w, base_h))

    encoded_img = base_img.copy()
    encoded_img[:, :, 0] = (base_img[:, :, 0] & 0xF8) | (hidden_resized[:, :, 0] >> 5)
    encoded_img[:, :, 1] = (base_img[:, :, 1] & 0xFC) | (hidden_resized[:, :, 1] >> 6)
    encoded_img[:, :, 2] = (base_img[:, :, 2] & 0xF8) | (hidden_resized[:, :, 2] >> 5)

    cv2.imwrite(output_path, encoded_img)

    psnr_value = psnr(base_img, encoded_img)
    ssim_value = ssim(base_img, encoded_img, multichannel=True)

    return output_path, psnr_value, ssim_value

def decode_image(encoded_img_path, output_path):
    encoded_img = cv2.imread(encoded_img_path)

    if encoded_img is None:
        return "Error: Encoded image could not be loaded."

    decoded_img = np.zeros_like(encoded_img)

    decoded_img[:, :, 0] = (encoded_img[:, :, 0] & 0x07) << 5
    decoded_img[:, :, 1] = (encoded_img[:, :, 1] & 0x03) << 6
    decoded_img[:, :, 2] = (encoded_img[:, :, 2] & 0x07) << 5

    cv2.imwrite(output_path, decoded_img)

    return output_path
