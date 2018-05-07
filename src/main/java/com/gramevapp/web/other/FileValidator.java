package com.gramevapp.web.other;

import com.gramevapp.web.model.FileModelDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class FileValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return FileModelDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        FileModelDto fileModelDto = (FileModelDto) target;

        if (fileModelDto.getTypeFile() != null && fileModelDto.getTypeFile().isEmpty()){
            errors.rejectValue("file", "file.empty");
        }
    }
}