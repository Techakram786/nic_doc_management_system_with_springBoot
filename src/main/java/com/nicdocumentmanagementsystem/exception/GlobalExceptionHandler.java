package com.nicdocumentmanagementsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(
            MaxUploadSizeExceededException exc,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "Upload failed: File size cannot exceed 10 MB.");

        // Redirect back to the previous page (the upload form)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public String handleFileAlreadyExistsException(
            FileAlreadyExistsException exc,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", exc.getMessage());

        // Redirect back to the previous page (the upload form)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccessException(
            DataAccessException exc,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String message = "An unexpected database error occurred. Please try again.";
        if (exc.getMostSpecificCause().getMessage().contains("Data too long for column")) {
            message = "Database error: The text for one of the fields is too long.";
        }

        redirectAttributes.addFlashAttribute("message", message);

        // Redirect back to the previous page (the upload form)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
