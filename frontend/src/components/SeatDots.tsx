interface Props {
  max: number;
  taken: number;
  size?: 'sm' | 'lg';
  closed?: boolean;
  /** 방금 채워진 좌석 인덱스. 채워지는 애니메이션을 준다. */
  highlight?: number | null;
}

/** 정원을 의자 모양 칸으로 표시. 이 서비스의 핵심 시각 요소. */
export function SeatDots({ max, taken, size = 'sm', closed = false, highlight = null }: Props) {
  const seats = Array.from({ length: max }, (_, i) => i);
  return (
    <div
      className={`seats seats--${size}${closed ? ' seats--closed' : ''}`}
      role="img"
      aria-label={`정원 ${max}명 중 ${taken}명 참여`}
    >
      {seats.map((i) => (
        <span
          key={i}
          className={`seat${i < taken ? ' seat--taken' : ''}${i === highlight ? ' seat--new' : ''}`}
        />
      ))}
    </div>
  );
}
