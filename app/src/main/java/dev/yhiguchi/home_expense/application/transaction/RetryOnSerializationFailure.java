package dev.yhiguchi.home_expense.application.transaction;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * トランザクションが直列化失敗 (SQLSTATE 40001) で中断された場合に、トランザクション全体を再実行する。
 *
 * <p>CockroachDB は SERIALIZABLE 分離レベルで競合を検出すると 40001 を返し、クライアントに再試行を求める。再試行は
 * トランザクションの外側で行う必要があるため、{@link jakarta.transaction.Transactional} と併用する。
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RetryOnSerializationFailure {}
