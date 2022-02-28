package services;

import com.google.inject.Inject;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import querries.SqlQuerries;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(DataServiceVerticle.class);
    private final PgPool pgPool;

    @Inject
    public DataServiceVerticle(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.all.service", this::getAllHandler);
    }

    private void getAllHandler(Message<JsonArray> msg) {

        List<String> columnNames = new ArrayList<>();
        String keyword = msg.body()
                .getJsonObject(0)
                .getString("keyword");

        pgPool.preparedQuery(SqlQuerries.getAllQuerry(keyword))
                .rxExecute()
                .map(rowSet -> {
                    columnNames.addAll(rowSet.columnsNames());
                    return rowSet;
                })
                .flatMapObservable(Observable::fromIterable)
                .map(row -> this.addRowToJson(columnNames, row))
                .toList()
                .subscribe(list -> {
                    LOGGER.info("Got all " + keyword + " from db");
                    msg.reply(new JsonArray(list));
                }, error -> {
                    LOGGER.error(error);
                    msg.fail(500, error.getMessage());
                });
    }

    private JsonObject addRowToJson(List<String> columnNames, Row row) {
        JsonObject jsonObject = new JsonObject();

        columnNames.forEach(columnName -> jsonObject.put(columnName,
                row.getValue(columnName) instanceof LocalDateTime ?
                        row.getValue(columnName).toString() : row.getValue(columnName)));

        return jsonObject;
    }
}