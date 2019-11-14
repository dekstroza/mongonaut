package io.dekstroza;

import io.micrometer.core.annotation.Timed;
import io.micronaut.core.version.annotation.Version;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static io.micronaut.http.MediaType.APPLICATION_JSON;


@Singleton
@Controller("/mongonaut")
public class AlarmControllerImpl implements AlarmController {

    private final AlarmService alarmService;

    private static final Logger log = LoggerFactory.getLogger(AlarmControllerImpl.class);

    @Inject
    public AlarmControllerImpl(final AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    /**
     * Get all alarms from database
     *
     * @return Returns json array of all alarms in the database
     */
    @Timed(value = "method.alarms.api.getall", percentiles = { 0.5, 0.95, 0.99 }, description = "Read all alarms api metric")
    @Get(value = "/alarms", produces = APPLICATION_JSON)
    @Version("1")
    public Flowable<Alarm> getAll() {
        return alarmService.getAll();
    }

    /**
     * Save alarm to the database
     *
     * @param body
     *            Alarm to be saved
     * @return Persisted alarm and it's location url
     */
    @Timed(value = "method.alarms.api.save", percentiles = { 0.5, 0.95, 0.99 }, description = "Insert alarm api metric")
    @Post(value = "/alarms", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    @NewSpan("mongonaut-service")
    @Version("1")
    public Single<HttpResponse<Alarm>> save(@SpanTag("alarm.id") @Body @NotNull Single<Alarm> body) {
        return body.flatMap(alarmService::save).flatMap(alarm -> Single.just(HttpResponse.created(alarm)));
    }

    /**
     * Find alarm by id
     *
     * @param id
     *            Integer representing id of this alarm
     * @return Alarm with this id
     */
    @Get(value = "/alarms/{id}", produces = APPLICATION_JSON)
    @Timed(value = "method.alarms.api.findById", percentiles = { 0.5, 0.95, 0.99 }, description = "Find alarm by id api metric")
    @Version("1")
    public Flowable<Alarm> findById(@PathVariable("id") Integer id) {
        return alarmService.findById(id);

    }

    /**
     * Find alarms by severity level
     *
     * @param severity
     *            Requested severity level, can be LOW, MEDIUM or CRITICAL
     * @return All alarms with requested level of severity as json array
     */
    @Get(value = "/alarms/severity/{severity}", produces = APPLICATION_JSON)
    @Timed(value = "method.alarms.api.findBySeverity", percentiles = { 0.5, 0.95, 0.99 }, description = "Find alarm by severity api metric")
    @Version("1")
    public Flowable<Alarm> findBySeverity(@NotBlank String severity) {
        return alarmService.findAlarmsBySeverity(severity);
    }

}
