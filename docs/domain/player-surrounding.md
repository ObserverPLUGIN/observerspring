# Player Surrounding Domain Design

## Goal
- 웹에서 플레이어 주변을 `발밑`, `몸높이`, `머리위` 3개 레이어로 조회한다.
- 쓰기 부하는 높고 조회는 "최신 스냅샷 1건" 중심이므로, 레이어 셀은 Oracle `CLOB` JSON으로 저장한다.

## Aggregate
- `MinecraftPlayer`
  - 플레이어 식별자와 최신 라이브 상태를 가진다.
- `MinecraftWorld`
  - 서버명 + 월드명 조합으로 월드를 식별한다.
- `PlayerSurroundingSnapshot`
  - 특정 시점의 플레이어 위치와 3개 레이어 묶음을 가진다.
- `PlayerSurroundingLayer`
  - 하나의 스냅샷에 속한 단면 1장을 저장한다.

## Table Intent
- `mc_players`
  - 플레이어 UUID, 최근 이름, 최근 좌표와 방향, 마지막 스냅샷 시각
- `mc_worlds`
  - 서버명, 월드명, 차원 타입
- `player_surrounding_snapshots`
  - 반경, 중심 좌표, 방향, 캡처 시각, 캡처 트리거
- `player_surrounding_layers`
  - 레이어 타입, 실제 Y, 상대 Y, 셀 JSON, 집계 카운트

## Why JSON CLOB
- `11 x 11 x 3` 셀을 전부 정규화하면 스냅샷 1건당 행 수가 급증한다.
- 이 화면은 분석보다 렌더링이 목적이라서, 레이어 JSON 한 덩어리로 읽는 편이 단순하고 빠르다.
- Oracle에서는 `CLOB`가 안정적이고 JPA에서도 무난하게 다룰 수 있다.

## Suggested cellsJson contract
```json
{
  "radius": 5,
  "size": 11,
  "rows": [
    [
      {
        "material": "STONE",
        "solid": true,
        "highlighted": false
      }
    ]
  ]
}
```

## Query pattern
- 플레이어 목록: `mc_players` 기준 최신 상태 조회
- 플레이어 상세: `findFirstByPlayer_PlayerUuidOrderByCapturedAtDesc`
- 상세 화면 렌더링: 최신 스냅샷의 `layers` 3건을 탭으로 전환
