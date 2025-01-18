import { Column, Grid } from '@carbon/react';
import CardItem from './card-item';

function ScrollableCardContainer() {
  const imageUrl = 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png';
  const sample = [
    { id: 1, name: 'test 1', imageUrl: imageUrl },
    { id: 2, name: 'test 1', imageUrl: imageUrl },
    { id: 3, name: 'test 1', imageUrl: imageUrl },
    { id: 4, name: 'test 1', imageUrl: imageUrl },
    { id: 5, name: 'test 1', imageUrl: imageUrl },
    { id: 6, name: 'test 1', imageUrl: imageUrl },
    { id: 7, name: 'test 1', imageUrl: imageUrl },
    { id: 8, name: 'test 1', imageUrl: imageUrl },
    { id: 9, name: 'test 1', imageUrl: imageUrl },
    { id: 10, name: 'test 1', imageUrl: imageUrl },
    { id: 11, name: 'test 1', imageUrl: imageUrl }
  ];

  return (
    <Grid fullWidth as={'div'} className={'accordion--grid-container'}>
      <>
        {sample.map((item) => (
          <Column key={item.id} span={4} style={{ marginBottom: '1rem' }}>
            <CardItem id={item.id} displayName={item.name} imageUrl={item.imageUrl} />
          </Column>
        ))}
      </>
    </Grid>
  );
}

export default ScrollableCardContainer;
