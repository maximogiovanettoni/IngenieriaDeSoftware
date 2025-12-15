import { useState } from 'react';
import { Upload, AlertCircle } from 'lucide-react';

interface ImageUploaderProps {
  onFileSelect: (file: File) => void;
  currentImageUrl?: string | null;
  maxSizeMB?: number;
  error?: string | null;
  previewUrl?: string | null;
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  onFileSelect,
  currentImageUrl,
  maxSizeMB = 5,
  error = null,
  previewUrl = null,
}) => {
  const [localError, setLocalError] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setLocalError(null);

    // Validate file size
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      setLocalError(`La imagen no puede superar los ${maxSizeMB} MB.`);
      e.target.value = '';
      return;
    }

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setLocalError('Por favor selecciona un archivo de imagen válido.');
      e.target.value = '';
      return;
    }

    onFileSelect(file);
  };

  const displayError = localError || error;

  return (
    <div className="image-uploader">
      <div className="image-uploader__preview-container">
        {previewUrl && (
          <div className="image-uploader__preview">
            <img src={previewUrl} alt="Preview" />
          </div>
        )}
        {!previewUrl && currentImageUrl && (
          <div className="image-uploader__preview">
            <img src={currentImageUrl} alt="Current" />
          </div>
        )}
        {!previewUrl && !currentImageUrl && (
          <div className="image-uploader__placeholder">
            <Upload size={48} />
            <p>Sin imagen</p>
          </div>
        )}
      </div>

      <div className="image-uploader__input-wrapper">
        <label htmlFor="image-input" className="image-uploader__label">
          <Upload size={20} />
          <span>Seleccionar imagen</span>
        </label>
        <input
          id="image-input"
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          className="image-uploader__input"
        />
      </div>

      {displayError && (
        <div className="image-uploader__error">
          <AlertCircle size={18} />
          <span>{displayError}</span>
        </div>
      )}

      <p className="image-uploader__info">
        Máximo {maxSizeMB}MB. Formatos soportados: JPG, PNG, GIF, WebP
      </p>
    </div>
  );
};
