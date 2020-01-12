package com.github.cndjp.godfather.endpoint.utils.finchx

import io.finch.Endpoint

/**
 * finchx の機能を使用可能にするために必要なトレイトです．
 * finchx に関する依存関係は一手に Endpoint.Module によって担われているようです．
 * 具体的には
 * ・get/put/patch などの finch 独自の DSL を使用可能にする
 * ・Endpoint の多相化（`Endpoint[IO, String]` を可能にする）
 * などがあります．各エンドポイントには必ずこれを MixIn してください．
 *
 * @tparam F 型コンストラクタ．
 */
trait FinchxEndpointOps[F[_]] extends Endpoint.Module[F]
