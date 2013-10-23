package org.opendatakit.aggregate.externalservice;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.opendatakit.common.utils.WebUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * TODO: comment and deal with null/empty value args
 *
 * @author the.dylan.price@gmail.com
 *
 */
public class OhmageJsonTypes {

  public static final class Survey implements JsonSerializer<Survey> {
    /**
     * a string in the ISO 8601 format to the detail of seconds:
     *
     * YYYY-MM-DD hh:mm:ss.
     */
    private String date;

    /** an int specifying the number of milliseconds since the epoch. */
    private Long time;

    /** a string representing a standard time zone. */
    private String timezone;

    /**
     * a string describing location status. Must be one of: unavailable, valid,
     * inaccurate, stale. If the status is unavailable, it is an error to send a
     * location object.
     */
    private String location_status;

    /** an object for housing location data */
    private Location location;

    /**
     * a string defining a survey in the campaign's associated configuration
     * file at the XPath /surveys/survey/id
     */
    private String survey_id;

    /**
     * an object with variable properties that describes the survey's launch
     * context. See the trigger framework page for a description of the object's
     * contents. The object must contain the property launch_time.
     */
    private String survey_launch_context;

    /** an array composed of prompt responses and/or repeatable sets. */
    private List<Response> responses;

    /**
     * @return the date
     */
    public String getDate() {
      return date;
    }

    /**
     * @param date
     *          the date to set
     */
    public void setDate(String date) {
      this.date = date;
    }

    /**
     * @return the time
     */
    public Long getTime() {
      return time;
    }

    /**
     * @param time
     *          the time to set
     */
    public void setTime(Long time) {
      this.time = time;
    }

    /**
     * @return the timezone
     */
    public String getTimezone() {
      return timezone;
    }

    /**
     * @param timezone
     *          the timezone to set
     */
    public void setTimezone(String timezone) {
      this.timezone = timezone;
    }

    /**
     * @return the location_status
     */
    public String getLocation_status() {
      return location_status;
    }

    /**
     * @param location_status
     *          the location_status to set
     */
    public void setLocation_status(String location_status) {
      this.location_status = location_status;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
      return location;
    }

    /**
     * @param location
     *          the location to set
     */
    public void setLocation(Location location) {
      this.location = location;
    }

    /**
     * @return the survey_id
     */
    public String getSurvey_id() {
      return survey_id;
    }

    /**
     * @param survey_id
     *          the survey_id to set
     */
    public void setSurvey_id(String survey_id) {
      this.survey_id = survey_id;
    }

    /**
     * @return the survey_lauch_context
     */
    public String getSurvey_lauch_context() {
      return survey_launch_context;
    }

    /**
     * @param survey_lauch_context
     *          the survey_lauch_context to set
     */
    public void setSurvey_lauch_context(String survey_lauch_context) {
      this.survey_launch_context = survey_lauch_context;
    }

    /**
     * @return the responses
     */
    public List<Response> getResponses() {
      return responses;
    }

    /**
     * @param responses
     *          the responses to set
     */
    public void setResponses(List<Response> responses) {
      this.responses = responses;
    }

    @Override
    public JsonElement serialize(Survey src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.addProperty("date", src.date);
      obj.addProperty("time", src.time);
      obj.addProperty("timezone", src.timezone);
      obj.addProperty("location_status", src.location_status);
      obj.add("location", context.serialize(src.location));
      obj.addProperty("survey_id", src.survey_id);
      obj.addProperty("survey_launch_context", src.survey_launch_context);
      JsonArray responsesArray = new JsonArray();
      if (src.responses != null) {
        for (Response response : src.responses) {
          responsesArray.add(context.serialize(response));
        }
      }
      obj.add("responses", responsesArray);
      return obj;
    }

  }

  @SuppressWarnings("unused")
  public static final class Location {
    /** a double representing latitude. */
    private Double latitude;

    /** a double representing latitude. */
    private Double longitude;

    /** a non-empty string (e.g., GPS or network). */
    private String provider;

    /** a float representing accuracy in meters from actual location. */
    private Double accuracy;

    /** the timestamp at which the location data was generated. */
    private String timestamp;
  }

  public abstract static class Response {
  }

  public static final class RepeatableSet extends Response implements JsonSerializer<RepeatableSet> {
    /** the id/label for a repeatable set of prompts */
    private final String repeatable_set_id;

    /**
     * true/false. identifies that the extra iterations of the repeatable set
     * were skipped (the end user refused to answer another iteration instead of
     * just stopping normally). required.
     */
    private final Boolean skipped;

    /**
     * true/false. If true, identifies that the repeatable set was not displayed
     * to the end user. If true, the responses element must be an empty array.
     */
    private final Boolean not_displayed;

    /**
     * prompt_id-value pairs. Each repeatable set iteration is grouped together
     * in an array.
     */
    private final List<List<Response>> responses;

    // for gson
    public RepeatableSet() {
      this.repeatable_set_id = null;
      this.skipped = null;
      this.not_displayed = null;
      this.responses = null;
    }

    /**
     * @param repeatable_set_id
     * @param skipped
     * @param not_displayed
     */
    public RepeatableSet(String repeatable_set_id, boolean skipped, boolean not_displayed) {
      this.repeatable_set_id = repeatable_set_id;
      this.skipped = skipped;
      this.not_displayed = not_displayed;
      this.responses = new ArrayList<List<Response>>();
    }

    public void addRepeatableSetIteration(List<Response> iteration) {
      this.responses.add(iteration);
    }

    /**
     * @return the repeatable_set_id
     */
    public String getRepeatable_set_id() {
      return repeatable_set_id;
    }

    /**
     * @return the skipped
     */
    public boolean isSkipped() {
      return skipped;
    }

    /**
     * @return the not_displayed
     */
    public boolean isNot_displayed() {
      return not_displayed;
    }

    /**
     * @return the responses
     */
    public List<List<Response>> getResponses() {
      return responses;
    }

    @Override
    public JsonElement serialize(RepeatableSet src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.addProperty("repeatable_set_id", src.repeatable_set_id);
      obj.addProperty("skipped", src.skipped);
      obj.addProperty("not_displayed", src.not_displayed);
      JsonArray responsesArray = new JsonArray();
      if (src.responses != null) {
        for (List<Response> responseList : src.responses) {
          JsonArray innerArray = new JsonArray();
          for (Response response : responseList) {
            innerArray.add(context.serialize(response));
          }
          responsesArray.add(innerArray);
        }
      }
      obj.add("responses", responsesArray);
      return obj;
    }
  }

  public abstract static class PromptResponse extends Response {
    /** the prompt id */
    private final String prompt_id;

    protected PromptResponse(String prompt_id) {
      this.prompt_id = prompt_id;
    }

    /**
     * @return the prompt_id
     */
    public String getPrompt_id() {
      return this.prompt_id;
    }
  }

  @SuppressWarnings("unused")
  public static final class timestamp extends PromptResponse {
    private final String value;

    // for gson
    private timestamp() {
      super(null);
      this.value = null;
    }

    public timestamp(String prompt_id, Date value) {
      super(prompt_id);
      this.value = WebUtils.iso8601Date(value);
    }
  }

  @SuppressWarnings("unused")
  public static final class number extends PromptResponse {
    private final Long value;

    // for gson
    private number() {
      super(null);
      this.value = null;
    }

    public number(String prompt_id, Long value) {
      super(prompt_id);
      this.value = value;
    }
  }

  public static final class text extends PromptResponse {
    /** format depends on the response type for each prompt */
    private final String value;

    // for gson
    private text() {
      super(null);
      this.value = null;
    }

    /**
     * @param prompt_id
     * @param value
     */
    public text(String prompt_id, String value) {
      super(prompt_id);
      this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
      return value;
    }
  }

  public static final class multi_choice_custom extends PromptResponse {
    private final Integer[] value;
    private final custom_choice[] custom_choices;

    // for gson
    private multi_choice_custom() {
      super(null);
      this.value = null;
      this.custom_choices = null;
    }

    public multi_choice_custom(String prompt_id, List<String> choices, List<String> possibleChoices) {
      super(prompt_id);

      this.value = new Integer[choices.size()];
      this.custom_choices = new custom_choice[possibleChoices.size()];

      for (int i = 0; i < choices.size(); i++) {
        this.value[i] = possibleChoices.indexOf(choices.get(i));
      }
      for (int i = 0; i < possibleChoices.size(); i++) {
        this.custom_choices[i] = new custom_choice(i, possibleChoices.get(i));
      }
    }

  }

  @SuppressWarnings("unused")
  public static final class single_choice_custom extends PromptResponse {
    private final Integer value;
    private final custom_choice[] custom_choices;

    // for gson
    private single_choice_custom() {
      super(null);
      this.value = null;
      this.custom_choices = null;
    }

    public single_choice_custom(String prompt_id, String choice, List<String> possibleChoices) {
      super(prompt_id);

      this.value = possibleChoices.indexOf(choice);
      this.custom_choices = new custom_choice[possibleChoices.size()];

      for (int i = 0; i < possibleChoices.size(); i++) {
        this.custom_choices[i] = new custom_choice(i, possibleChoices.get(i));
      }
    }
  }

  /**
   * Used by multi_choice_custom and single_choice_custom.
   */
  @SuppressWarnings("unused")
  private static final class custom_choice {
    private final Integer choice_id;
    private final String choice_value;

    // for gson
    private custom_choice() {
      this.choice_id = null;
      this.choice_value = null;
    }

    custom_choice(Integer choice_id, String choice_value) {
      this.choice_id = choice_id;
      this.choice_value = choice_value;
    }
  }

  @SuppressWarnings("unused")
  public static final class photo extends PromptResponse {
    private final String value;

    // for gson
    private photo() {
      super(null);
      this.value = null;
    }

    public photo(String prompt_id, UUID photoUUID) {
      super(prompt_id);
      this.value = photoUUID.toString();
    }
  }

  public static final class server_response {
    private String result;
    private error[] errors;

    /**
     * @return the result
     */
    public String getResult() {
      return result;
    }

    /**
     * @return the errors
     */
    public error[] getErrors() {
      return errors;
    }

  }

  /**
   * Used by server_response.
   */
  @SuppressWarnings("unused")
  private static final class error {
    private Integer code;
    private String text;

    /**
     * @return the code
     */
    public Integer getCode() {
      return code;
    }

    /**
     * @return the text
     */
    public String getText() {
      return text;
    }

  }
}
