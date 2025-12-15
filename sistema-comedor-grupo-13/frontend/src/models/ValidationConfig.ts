export type ValidationConfig = {
  email: {
    allowedEmailDomain: string;
    domainPattern: string;
    messages: {
      required: string;
      invalidFormat: string;
      invalidDomain: string;
    };
  };
  user: {
    birthDate: {
      minAge: number;
      maxAge: number;
      messages: {
        required: string;
        invalidFormat: string;
        invalidRange: string;
      };
    };
    firstName: {
      minLength: number;
      maxLength: number;
      pattern: string;
      messages: {
        required: string;
        invalidFormat: string;
        invalidLength: string;
      };
    };
    lastName: {
      minLength: number;
      maxLength: number;
      pattern: string;
      messages: {
        required: string;
        invalidFormat: string;
        invalidLength: string;
      };
    };
    password: {
      minLength: number;
      maxLength: number;
      pattern: string;
      messages: {
        required: string;
        invalidFormat: string;
        invalidLength: string;
      };
    };
    address: {
      maxLength: number;
      pattern: string;
      messages: {
        required: string;
        invalidFormat: string;
        invalidLength: string;
      };
    };
    gender: {
      pattern: string;
      messages: {
        required: string;
        invalidFormat: string;
        invalidValue: string;
      };
    };
  };
};
